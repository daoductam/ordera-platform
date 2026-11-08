package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.usecase.GetRewardsCatalogUseCase;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.mapper.RewardDtoMapper;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.CatalogGiftItemResponse;
import com.ordera.modules.reward.presentation.dto.CatalogVoucherItemResponse;
import com.ordera.modules.reward.presentation.dto.RewardsCatalogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * UseCase lấy danh mục phần thưởng có thể đổi bằng điểm trong tab "Get More Rewards" (Figma 07.04).
 * Đánh giá điểm khả dụng của hội viên để tính canAfford và pointsNeeded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetRewardsCatalogUseCaseImpl implements GetRewardsCatalogUseCase {

    private final MemberProfileJpaRepository memberProfileRepository;
    private final VoucherJpaRepository voucherRepository;
    private final LoyaltyGiftJpaRepository loyaltyGiftRepository;
    private final RewardDtoMapper rewardDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public RewardsCatalogResponse execute() {
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity profile = resolveMemberProfile(tenantId, userId);
        int availablePoints = profile.getCurrentPoints();

        List<CatalogVoucherItemResponse> voucherItems = buildVoucherCatalog(tenantId, availablePoints);
        List<CatalogGiftItemResponse> giftItems = buildGiftCatalog(tenantId, availablePoints);

        return RewardsCatalogResponse.builder()
                .memberAvailablePoints(availablePoints)
                .vouchers(voucherItems)
                .gifts(giftItems)
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

    private List<CatalogVoucherItemResponse> buildVoucherCatalog(UUID tenantId, int availablePoints) {
        List<VoucherJpaEntity> vouchers = voucherRepository.findByTenantIdAndActiveAndValidUntilAfter(tenantId, true, Instant.now());
        List<CatalogVoucherItemResponse> result = new ArrayList<>();
        for (VoucherJpaEntity v : vouchers) {
            CatalogVoucherItemResponse dto = rewardDtoMapper.toCatalogVoucherItem(v);
            boolean canAfford = availablePoints >= v.getPointsCost();
            dto.setCanAfford(canAfford);
            dto.setPointsNeeded(canAfford ? 0 : v.getPointsCost() - availablePoints);
            result.add(dto);
        }
        return result;
    }

    private List<CatalogGiftItemResponse> buildGiftCatalog(UUID tenantId, int availablePoints) {
        List<LoyaltyGiftJpaEntity> gifts = loyaltyGiftRepository.findByTenantIdAndActive(tenantId, true);
        List<CatalogGiftItemResponse> result = new ArrayList<>();
        for (LoyaltyGiftJpaEntity g : gifts) {
            CatalogGiftItemResponse dto = rewardDtoMapper.toCatalogGiftItem(g);
            boolean canAfford = availablePoints >= g.getPointsCost();
            dto.setCanAfford(canAfford);
            dto.setPointsNeeded(canAfford ? 0 : g.getPointsCost() - availablePoints);
            result.add(dto);
        }
        return result;
    }
}
