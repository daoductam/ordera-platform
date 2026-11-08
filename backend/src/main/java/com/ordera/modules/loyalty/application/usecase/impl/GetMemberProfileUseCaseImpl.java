package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.LoyaltyTierUtils;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.loyalty.application.usecase.GetMemberProfileUseCase;
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
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(LoyaltyErrorCode.TENANT_HEADER_MISSING);
        }

        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(LoyaltyErrorCode.UNAUTHORIZED);
        }

        MemberProfileJpaEntity profile = memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));

        UserJpaEntity user = userRepository.findByIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));

        List<TierDefinitionJpaEntity> tiers = tierDefinitionRepository.findByTenantIdOrderByTierOrderAsc(tenantId);
        LoyaltyTierUtils.TierProgressResult progress = LoyaltyTierUtils.calculateTierProgress(
                profile.getCurrentTierId(),
                profile.getCurrentPoints(),
                profile.getLifetimePoints(),
                tiers
        );

        boolean hasMissingNotice = supportClaimRepository.existsByTenantIdAndMemberIdAndStatus(tenantId, profile.getId(), "PENDING");

        String barcode = profile.getBarcodeToken() != null ? profile.getBarcodeToken() : "ORD-MEM-" + profile.getId().toString().substring(0, 8).toUpperCase();
        String barcodeQrUrl = "https://api.ordera.no/v1/barcodes/" + barcode + ".png";

        LoyaltyTierInfoResponse loyaltyInfo = LoyaltyTierInfoResponse.builder()
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

        QuickActionCountersResponse counters = QuickActionCountersResponse.builder()
                .activeVouchersCount(2)
                .availableGiftsCount(1)
                .activeOrdersCount(1)
                .unreadNotificationsCount(hasMissingNotice ? 1 : 0)
                .build();

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

