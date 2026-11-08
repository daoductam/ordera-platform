package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.LoyaltyTierUtils;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.loyalty.application.usecase.GetMemberProfileUseCase;
import com.ordera.modules.loyalty.domain.constant.LoyaltyConstants;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.SupportClaimJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.loyalty.presentation.dto.LoyaltyTierInfoResponse;
import com.ordera.modules.loyalty.presentation.dto.MemberProfileResponse;
import com.ordera.modules.loyalty.presentation.dto.QuickActionCountersResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lấy toàn bộ thông tin chi tiết hồ sơ hội viên và tiến độ thẻ thành viên.
 * Áp dụng Orchestrator Pattern điều phối các bước truy vấn dữ liệu.
 * Tái sử dụng hằng số cấu hình từ LoyaltyConstants.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetMemberProfileUseCaseImpl implements GetMemberProfileUseCase {

    private final MemberProfileJpaRepository memberProfileRepository;
    private final TierDefinitionJpaRepository tierDefinitionRepository;
    private final SupportClaimJpaRepository supportClaimRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public MemberProfileResponse execute() {
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();

        MemberProfileJpaEntity profile = fetchMemberProfile(tenantId, userId);
        UserJpaEntity user = fetchUser(tenantId, userId);

        LoyaltyTierInfoResponse loyaltyInfo = buildTierInfo(tenantId, profile);
        boolean hasMissingNotice = checkMissingPointsNotice(tenantId, profile.getId());
        QuickActionCountersResponse counters = buildCounters(hasMissingNotice);

        String barcode = resolveBarcode(profile);
        String barcodeQrUrl = String.format(
                LoyaltyConstants.BARCODE_URL_TEMPLATE,
                LoyaltyConstants.DEFAULT_API_BASE_URL,
                barcode
        );

        return buildResponse(profile, user, loyaltyInfo, counters, barcode, barcodeQrUrl, hasMissingNotice);
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(LoyaltyErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private UUID resolveUserId() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(LoyaltyErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    private MemberProfileJpaEntity fetchMemberProfile(UUID tenantId, UUID userId) {
        return memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }

    private UserJpaEntity fetchUser(UUID tenantId, UUID userId) {
        return userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }

    private LoyaltyTierInfoResponse buildTierInfo(UUID tenantId, MemberProfileJpaEntity profile) {
        List<TierDefinitionJpaEntity> tiers = tierDefinitionRepository.findByTenantIdOrderByTierOrderAsc(tenantId);
        LoyaltyTierUtils.TierProgressResult progress = LoyaltyTierUtils.calculateTierProgress(
                profile.getCurrentTierId(),
                profile.getCurrentPoints(),
                profile.getLifetimePoints(),
                tiers
        );

        return LoyaltyTierInfoResponse.builder()
                .currentTier(progress.currentTierName())
                .tierOrder(progress.currentTierOrder())
                .themeColorHex(progress.themeColorHex())
                .earnPointsRate(progress.earnPointsRate())
                .currentPoints(progress.currentPoints())
                .lifetimePoints(progress.lifetimePoints())
                .pointsToNextTier(progress.pointsToNextTier())
                .nextTierName(progress.nextTierName())
                .nextTierMinPoints(progress.nextTierMinPoints())
                .progressPercentage(progress.progressPercentage())
                .build();
    }

    private boolean checkMissingPointsNotice(UUID tenantId, UUID memberId) {
        return supportClaimRepository.existsByTenantIdAndMemberIdAndStatus(
                tenantId, memberId, LoyaltyConstants.CLAIM_STATUS_PENDING
        );
    }

    private QuickActionCountersResponse buildCounters(boolean hasMissingNotice) {
        return QuickActionCountersResponse.builder()
                .activeVouchersCount(LoyaltyConstants.DEFAULT_ACTIVE_VOUCHERS_COUNT)
                .availableGiftsCount(LoyaltyConstants.DEFAULT_AVAILABLE_GIFTS_COUNT)
                .activeOrdersCount(LoyaltyConstants.DEFAULT_ACTIVE_ORDERS_COUNT)
                .unreadNotificationsCount(hasMissingNotice ? 1 : 0)
                .build();
    }

    private String resolveBarcode(MemberProfileJpaEntity profile) {
        if (profile.getBarcodeToken() != null && !profile.getBarcodeToken().isBlank()) {
            return profile.getBarcodeToken();
        }
        return LoyaltyConstants.DEFAULT_BARCODE_PREFIX + profile.getId().toString().substring(0, 8).toUpperCase();
    }

    private MemberProfileResponse buildResponse(
            MemberProfileJpaEntity profile,
            UserJpaEntity user,
            LoyaltyTierInfoResponse loyaltyInfo,
            QuickActionCountersResponse counters,
            String barcode,
            String barcodeQrUrl,
            boolean hasMissingNotice
    ) {
        return MemberProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .avatarUrl(profile.getAvatarUrl())
                .phoneVerified(user.isPhoneVerified())
                .emailVerified(user.isEmailVerified())
                .barcodeToken(barcode)
                .barcodeQrUrl(barcodeQrUrl)
                .loyalty(loyaltyInfo)
                .quickActionCounters(counters)
                .hasMissingPointsNotice(hasMissingNotice)
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
