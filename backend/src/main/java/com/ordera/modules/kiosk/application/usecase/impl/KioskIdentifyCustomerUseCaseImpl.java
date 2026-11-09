package com.ordera.modules.kiosk.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.kiosk.application.usecase.KioskIdentifyCustomerUseCase;
import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.domain.model.KioskSession;
import com.ordera.modules.kiosk.infrastructure.adapter.RedisKioskSessionAdapter;
import com.ordera.modules.kiosk.infrastructure.persistence.repository.KioskDeviceJpaRepository;
import com.ordera.modules.kiosk.presentation.dto.request.KioskIdentifyRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskCustomerInfoResponse;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Orchestrator UseCase: Nhận diện khách hàng qua số điện thoại tại quầy Kiosk.
 * Hỗ trợ tra cứu hội viên hoặc tạo phiên tạm thời cho khách mới (Guest).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KioskIdentifyCustomerUseCaseImpl implements KioskIdentifyCustomerUseCase {

    private final KioskDeviceJpaRepository kioskDeviceRepository;
    private final UserRepository userRepository;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final TierDefinitionJpaRepository tierDefinitionRepository;
    private final UserVoucherJpaRepository userVoucherRepository;
    private final RedisKioskSessionAdapter redisKioskSessionAdapter;

    private static final Pattern PHONE_PATTERN = Pattern.compile(KioskConstants.PHONE_REGEX);

    @Override
    @Transactional(readOnly = true)
    public KioskCustomerInfoResponse execute(UUID tenantId, String deviceId, KioskIdentifyRequest request) {
        validateInputs(tenantId, deviceId, request);
        validateKioskDevice(tenantId, deviceId);

        String normalizedPhone = request.getPhoneNumber().trim();
        Optional<UserJpaEntity> userOpt = userRepository.findByTenantIdAndPhoneNumber(tenantId, normalizedPhone);

        if (userOpt.isEmpty()) {
            return handleUnregisteredCustomer(tenantId, request.getStoreId(), deviceId, normalizedPhone);
        }

        return handleRegisteredCustomer(tenantId, request.getStoreId(), deviceId, userOpt.get());
    }

    private void validateInputs(UUID tenantId, String deviceId, KioskIdentifyRequest request) {
        Objects.requireNonNull(tenantId, "TenantId must not be null");
        Objects.requireNonNull(deviceId, "DeviceId must not be null");
        Objects.requireNonNull(request, "Request must not be null");

        String phone = request.getPhoneNumber();
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new BusinessException(KioskErrorCode.INVALID_PHONE_NUMBER);
        }
    }

    private void validateKioskDevice(UUID tenantId, String deviceId) {
        boolean exists = kioskDeviceRepository.existsByTenantIdAndDeviceCode(tenantId, deviceId);
        if (!exists) {
            throw new BusinessException(KioskErrorCode.KIOSK_DEVICE_NOT_FOUND);
        }
    }

    private KioskCustomerInfoResponse handleRegisteredCustomer(UUID tenantId, UUID storeId, String deviceId, UserJpaEntity user) {
        MemberProfileJpaEntity profile = memberProfileRepository.findByTenantIdAndUserId(tenantId, user.getId())
                .or(() -> memberProfileRepository.findByIdAndTenantId(user.getId(), tenantId))
                .orElse(null);

        String tierName = KioskConstants.DEFAULT_TIER_NAME;
        double earnRate = KioskConstants.DEFAULT_EARN_RATE_PERCENT;
        int currentPoints = (profile != null) ? profile.getCurrentPoints() : KioskConstants.ZERO_POINTS;
        int lifetimePoints = (profile != null) ? profile.getLifetimePoints() : KioskConstants.ZERO_POINTS;

        if (profile != null && profile.getCurrentTierId() != null) {
            Optional<TierDefinitionJpaEntity> tierOpt = tierDefinitionRepository.findById(profile.getCurrentTierId());
            if (tierOpt.isPresent()) {
                tierName = tierOpt.get().getTierName();
                earnRate = tierOpt.get().getEarnPointsRate().doubleValue();
            }
        }

        int voucherCount = (int) userVoucherRepository.countByTenantIdAndMemberIdAndStatus(tenantId, user.getId(), "ACTIVE");

        cacheKioskSession(tenantId, storeId, deviceId, user.getId(), user.getPhoneNumber(), user.getFullName(), tierName, currentPoints, earnRate, true);

        return KioskCustomerInfoResponse.builder()
                .isRegistered(true)
                .userId(user.getId())
                .name(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .currentTier(tierName)
                .currentPoints(currentPoints)
                .lifetimePoints(lifetimePoints)
                .earnRatePercent(earnRate)
                .availableVoucherCount(voucherCount)
                .build();
    }

    private KioskCustomerInfoResponse handleUnregisteredCustomer(UUID tenantId, UUID storeId, String deviceId, String phone) {
        cacheKioskSession(tenantId, storeId, deviceId, null, phone, KioskConstants.DEFAULT_GUEST_NAME,
                KioskConstants.DEFAULT_TIER_NAME, KioskConstants.ZERO_POINTS, KioskConstants.DEFAULT_EARN_RATE_PERCENT, false);

        return KioskCustomerInfoResponse.builder()
                .isRegistered(false)
                .userId(null)
                .name(KioskConstants.DEFAULT_GUEST_NAME)
                .phoneNumber(phone)
                .currentTier(KioskConstants.DEFAULT_TIER_NAME)
                .currentPoints(KioskConstants.ZERO_POINTS)
                .lifetimePoints(KioskConstants.ZERO_POINTS)
                .earnRatePercent(KioskConstants.DEFAULT_EARN_RATE_PERCENT)
                .availableVoucherCount(KioskConstants.DEFAULT_AVAILABLE_VOUCHERS)
                .build();
    }

    private void cacheKioskSession(UUID tenantId, UUID storeId, String deviceId, UUID userId, String phone,
                                   String name, String tier, int points, double earnRate, boolean registered) {
        KioskSession session = KioskSession.builder()
                .tenantId(tenantId)
                .storeId(storeId)
                .deviceCode(deviceId)
                .customerId(userId)
                .customerPhone(phone)
                .customerName(name)
                .currentTier(tier)
                .currentPoints(points)
                .earnRatePercent(earnRate)
                .registered(registered)
                .status(KioskConstants.KIOSK_STATUS_STANDBY)
                .createdAt(Instant.now())
                .lastActivityAt(Instant.now())
                .build();
        redisKioskSessionAdapter.saveSession(tenantId, deviceId, session);
    }
}
