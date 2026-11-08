package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.usecase.GetRewardsSummaryUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserGiftRedemptionJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.RewardsSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * UseCase lấy số lượng voucher và quà tặng đang có hiệu lực để render huy hiệu đỏ tab My Rewards (Figma 07.02).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetRewardsSummaryUseCaseImpl implements GetRewardsSummaryUseCase {

    private final MemberProfileJpaRepository memberProfileRepository;
    private final UserVoucherJpaRepository userVoucherRepository;
    private final UserGiftRedemptionJpaRepository userGiftRedemptionRepository;

    @Override
    @Transactional(readOnly = true)
    public RewardsSummaryResponse execute() {
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity profile = resolveMemberProfile(tenantId, userId);

        int activeVouchers = countActiveVouchers(tenantId, profile.getId());
        int availableGifts = countAvailableGifts(tenantId, profile.getId());
        int total = activeVouchers + availableGifts;

        return RewardsSummaryResponse.builder()
                .totalAvailableRewards(total)
                .activeVouchersCount(activeVouchers)
                .availableGiftsCount(availableGifts)
                .build();
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(RewardErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private UUID resolveUserId() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(RewardErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    private MemberProfileJpaEntity resolveMemberProfile(UUID tenantId, UUID userId) {
        return memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }

    private int countActiveVouchers(UUID tenantId, UUID memberId) {
        List<UserVoucherJpaEntity> list = userVoucherRepository.findByTenantIdAndMemberIdAndStatus(
                tenantId, memberId, RewardConstants.STATUS_AVAILABLE);
        return list.size();
    }

    private int countAvailableGifts(UUID tenantId, UUID memberId) {
        List<UserGiftRedemptionJpaEntity> list = userGiftRedemptionRepository.findByTenantIdAndMemberIdAndStatus(
                tenantId, memberId, RewardConstants.STATUS_PENDING);
        return list.size();
    }
}
