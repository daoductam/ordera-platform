package com.ordera.modules.auth.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.application.port.OtpStoragePort;
import com.ordera.modules.auth.application.service.JwtTokenProviderService;
import com.ordera.modules.auth.application.usecase.VerifyOtpUseCase;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.TokenPair;
import com.ordera.modules.auth.infrastructure.persistence.entity.OtpAuditLogJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.OtpAuditLogRepository;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.auth.presentation.dto.AuthUserResponse;
import com.ordera.modules.auth.presentation.dto.VerifyOtpRequest;
import com.ordera.modules.auth.presentation.dto.VerifyOtpResponse;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Hiện thực hóa VerifyOtpUseCase theo Orchestrator Pattern.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyOtpUseCaseImpl implements VerifyOtpUseCase {

    private final OtpStoragePort otpStoragePort;
    private final UserRepository userRepository;
    private final OtpAuditLogRepository otpAuditLogRepository;
    private final MemberProfileRepository memberProfileRepository;
    private final TierDefinitionRepository tierDefinitionRepository;
    private final JwtTokenProviderService jwtTokenProviderService;

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TTL_SECONDS = 900L; // 15 Phút

    @Override
    @Transactional
    public VerifyOtpResponse execute(VerifyOtpRequest request) {
        UUID tenantId = validateAndResolveTenant();
        validateRequest(request);
        checkAttemptLimits(tenantId, request.getPhoneNumber());

        verifyOtpCode(tenantId, request.getPhoneNumber(), request.getOtpCode());
        clearOtpCache(tenantId, request.getPhoneNumber());
        markAuditLogVerified(tenantId, request.getPhoneNumber());

        return resolveUserAndBuildResponse(tenantId, request.getPhoneNumber());
    }

    private UUID validateAndResolveTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(AuthErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private void validateRequest(VerifyOtpRequest request) {
        if (request == null || request.getPhoneNumber() == null || request.getOtpCode() == null) {
            throw new BusinessException(AuthErrorCode.VALIDATION_FAILED, "Dữ liệu xác thực không hợp lệ.");
        }
    }

    private void checkAttemptLimits(UUID tenantId, String phoneNumber) {
        int attempts = otpStoragePort.getAttempts(tenantId, phoneNumber);
        if (attempts >= MAX_ATTEMPTS) {
            throw new BusinessException(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
        }
    }

    private void verifyOtpCode(UUID tenantId, String phoneNumber, String otpCode) {
        Optional<String> cachedOtp = otpStoragePort.getOtp(tenantId, phoneNumber);
        if (cachedOtp.isEmpty()) {
            throw new BusinessException(AuthErrorCode.OTP_EXPIRED);
        }

        if (!cachedOtp.get().equals(otpCode)) {
            int currentAttempts = otpStoragePort.incrementAttempts(tenantId, phoneNumber, LOCK_TTL_SECONDS);
            if (currentAttempts >= MAX_ATTEMPTS) {
                throw new BusinessException(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED);
            }
            int remaining = Math.max(0, MAX_ATTEMPTS - currentAttempts);
            String message = String.format("Mã OTP không chính xác. Bạn còn %d lần thử.", remaining);
            throw new BusinessException(AuthErrorCode.OTP_INVALID, message, Map.of("remainingAttempts", remaining));
        }
    }

    private void clearOtpCache(UUID tenantId, String phoneNumber) {
        otpStoragePort.clearOtp(tenantId, phoneNumber);
    }

    private void markAuditLogVerified(UUID tenantId, String phoneNumber) {
        otpAuditLogRepository.findTopByTenantIdAndRecipientOrderByCreatedAtDesc(tenantId, phoneNumber)
                .ifPresent(logEntry -> {
                    logEntry.setVerified(true);
                    otpAuditLogRepository.save(logEntry);
                });
    }

    private VerifyOtpResponse resolveUserAndBuildResponse(UUID tenantId, String phoneNumber) {
        Optional<UserJpaEntity> userOpt = userRepository.findByTenantIdAndPhoneNumber(tenantId, phoneNumber);

        if (userOpt.isEmpty()) {
            // Người dùng chưa có tài khoản hoặc chưa hoàn thiện hồ sơ -> isProfileComplete = false
            return VerifyOtpResponse.builder()
                    .isProfileComplete(false)
                    .phoneNumber(phoneNumber)
                    .tempAuthToken("temp_auth_" + UUID.randomUUID())
                    .build();
        }

        UserJpaEntity user = userOpt.get();
        user.setPhoneVerified(true);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        TokenPair tokenPair = jwtTokenProviderService.generateTokenPair(user.getId(), tenantId, user.getRole());
        AuthUserResponse authUser = buildAuthUserResponse(tenantId, user);

        return VerifyOtpResponse.builder()
                .isProfileComplete(true)
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .tokenType(tokenPair.getTokenType())
                .expiresIn(tokenPair.getExpiresIn())
                .user(authUser)
                .build();
    }

    private AuthUserResponse buildAuthUserResponse(UUID tenantId, UserJpaEntity user) {
        AuthUserResponse.AuthUserResponseBuilder builder = AuthUserResponse.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .phoneVerified(user.isPhoneVerified())
                .emailVerified(user.isEmailVerified());

        memberProfileRepository.findByTenantIdAndUserId(tenantId, user.getId())
                .ifPresent(profile -> {
                    builder.dateOfBirth(profile.getDateOfBirth());
                    builder.currentPoints(profile.getCurrentPoints());
                    builder.lifetimePoints(profile.getLifetimePoints());
                    tierDefinitionRepository.findById(profile.getCurrentTierId())
                            .ifPresent(tier -> builder.tierName(tier.getTierName()));
                });

        return builder.build();
    }
}
