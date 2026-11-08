package com.ordera.modules.auth.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.application.port.OtpStoragePort;
import com.ordera.modules.auth.application.service.JwtTokenProviderService;
import com.ordera.modules.auth.application.usecase.VerifyRegisterOtpUseCase;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.TokenPair;
import com.ordera.modules.auth.domain.model.UserRole;
import com.ordera.modules.auth.domain.model.UserStatus;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.auth.presentation.dto.AuthUserResponse;
import com.ordera.modules.auth.presentation.dto.LoyaltyTierDetailDto;
import com.ordera.modules.auth.presentation.dto.VerifyRegisterOtpRequest;
import com.ordera.modules.auth.presentation.dto.VerifyRegisterOtpResponse;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Hiện thực hóa VerifyRegisterOtpUseCase theo Orchestrator Pattern.
 * Đăng ký tài khoản hội viên và tự động khởi tạo hồ sơ loyalty (New Member, 0 điểm).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyRegisterOtpUseCaseImpl implements VerifyRegisterOtpUseCase {

    private final UserRepository userRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final TierDefinitionRepository tierDefinitionRepository;
    private final OtpStoragePort otpStoragePort;
    private final JwtTokenProviderService jwtTokenProviderService;

    @Override
    @Transactional
    public VerifyRegisterOtpResponse execute(VerifyRegisterOtpRequest request) {
        UUID tenantId = validateAndResolveTenant();
        validateRequest(request);

        verifyRegistrationOtp(tenantId, request);
        ensureNotAlreadyRegistered(tenantId, request);

        UserJpaEntity user = createAndSaveUser(tenantId, request);
        TierDefinitionJpaEntity initialTier = resolveInitialTier(tenantId);
        MemberProfileJpaEntity profile = createAndSaveMemberProfile(tenantId, user.getId(), initialTier.getId(), request);

        TokenPair tokenPair = jwtTokenProviderService.generateTokenPair(user.getId(), tenantId, user.getRole());

        return buildResponse(user, profile, initialTier, tokenPair);
    }

    private UUID validateAndResolveTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(AuthErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private void validateRequest(VerifyRegisterOtpRequest request) {
        if (request == null || request.getPhoneNumber() == null || request.getOtpCode() == null) {
            throw new BusinessException(AuthErrorCode.VALIDATION_FAILED, "Dữ liệu đăng ký không hợp lệ.");
        }
    }

    private void verifyRegistrationOtp(UUID tenantId, VerifyRegisterOtpRequest request) {
        // Kiểm tra OTP theo Email trước, nếu không có thì kiểm tra theo Số điện thoại
        Optional<String> cachedOtp = otpStoragePort.getOtp(tenantId, request.getEmail());
        if (cachedOtp.isEmpty()) {
            cachedOtp = otpStoragePort.getOtp(tenantId, request.getPhoneNumber());
        }

        if (cachedOtp.isEmpty()) {
            throw new BusinessException(AuthErrorCode.OTP_EXPIRED);
        }

        if (!cachedOtp.get().equals(request.getOtpCode())) {
            throw new BusinessException(AuthErrorCode.OTP_INVALID);
        }

        // Xóa OTP sau khi kiểm tra hợp lệ
        otpStoragePort.clearOtp(tenantId, request.getEmail());
        otpStoragePort.clearOtp(tenantId, request.getPhoneNumber());
    }

    private void ensureNotAlreadyRegistered(UUID tenantId, VerifyRegisterOtpRequest request) {
        if (userRepository.existsByTenantIdAndPhoneNumber(tenantId, request.getPhoneNumber())
                || (request.getEmail() != null && userRepository.existsByTenantIdAndEmail(tenantId, request.getEmail()))) {
            throw new BusinessException(AuthErrorCode.USER_ALREADY_EXISTS);
        }
    }

    private UserJpaEntity createAndSaveUser(UUID tenantId, VerifyRegisterOtpRequest request) {
        String fullName = (request.getFullName() != null && !request.getFullName().isBlank())
                ? request.getFullName()
                : "Hội viên Ordera";

        UserJpaEntity user = UserJpaEntity.builder()
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .fullName(fullName)
                .phoneVerified(true)
                .emailVerified(true)
                .role(UserRole.CUSTOMER.name())
                .status(UserStatus.ACTIVE.name())
                .lastLoginAt(Instant.now())
                .build();
        user.setTenantId(tenantId);
        return userRepository.save(user);
    }

    private TierDefinitionJpaEntity resolveInitialTier(UUID tenantId) {
        // Tìm bậc 1 (New Member) của thương hiệu
        return tierDefinitionRepository.findByTenantIdAndTierOrder(tenantId, 1)
                .orElseGet(() -> tierDefinitionRepository.findByTenantIdOrderByTierOrderAsc(tenantId).stream()
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND, "Không tìm thấy cấu hình hạng thẻ khởi tạo")));
    }

    private MemberProfileJpaEntity createAndSaveMemberProfile(
            UUID tenantId, UUID userId, UUID tierId, VerifyRegisterOtpRequest request) {
        String cleanPhone = request.getPhoneNumber().replace("+", "");
        String barcodeToken = "ORD-MEM-" + cleanPhone;

        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentTierId(tierId)
                .currentPoints(0)
                .lifetimePoints(0)
                .barcodeToken(barcodeToken)
                .dateOfBirth(request.getDateOfBirth())
                .build();
        profile.setTenantId(tenantId);
        return memberProfileRepository.save(profile);
    }

    private VerifyRegisterOtpResponse buildResponse(
            UserJpaEntity user, MemberProfileJpaEntity profile, TierDefinitionJpaEntity tier, TokenPair tokenPair) {

        AuthUserResponse authUser = AuthUserResponse.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .phoneVerified(user.isPhoneVerified())
                .emailVerified(user.isEmailVerified())
                .dateOfBirth(profile.getDateOfBirth())
                .tierName(tier.getTierName())
                .currentPoints(profile.getCurrentPoints())
                .lifetimePoints(profile.getLifetimePoints())
                .build();

        LoyaltyTierDetailDto loyaltyDto = LoyaltyTierDetailDto.builder()
                .tierName(tier.getTierName())
                .tierOrder(tier.getTierOrder())
                .themeColorHex(tier.getThemeColorHex())
                .currentPoints(profile.getCurrentPoints())
                .lifetimePoints(profile.getLifetimePoints())
                .pointsToNextTier(200) // Khởi điểm lên Silver cần 200 điểm
                .nextTierName("Silver")
                .build();

        return VerifyRegisterOtpResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .tokenType(tokenPair.getTokenType())
                .expiresIn(tokenPair.getExpiresIn())
                .user(authUser)
                .loyalty(loyaltyDto)
                .build();
    }
}
