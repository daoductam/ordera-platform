package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.port.CartOfferPort;
import com.ordera.modules.reward.application.usecase.ApplyGiftToCartUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserGiftRedemptionJpaRepository;
import com.ordera.modules.reward.presentation.dto.ApplyGiftRequest;
import com.ordera.modules.reward.presentation.dto.CartGiftCalculationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Thêm quà tặng tri ân vào giỏ hàng (Figma 08.09, 08.10).
 * Chuyển trạng thái lượt đổi sang SELECTED, thực thi quy tắc tối đa 01 quà/đơn hàng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyGiftToCartUseCaseImpl implements ApplyGiftToCartUseCase {

    private final CartOfferPort cartOfferPort;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final LoyaltyGiftJpaRepository loyaltyGiftRepository;
    private final UserGiftRedemptionJpaRepository userGiftRedemptionRepository;

    @Override
    @Transactional
    public CartGiftCalculationResponse execute(ApplyGiftRequest request) {
        Objects.requireNonNull(request, "ApplyGiftRequest must not be null");
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity profile = resolveMemberProfile(tenantId, userId);

        CartOfferPort.CartOfferDetails cart = cartOfferPort.findCartByIdAndTenantId(request.getCartId(), tenantId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.CART_NOT_FOUND));

        validateCombinationRule(cart, request.getRewardGiftId());

        LoyaltyGiftJpaEntity gift = loyaltyGiftRepository.findByIdAndTenantId(request.getRewardGiftId(), tenantId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.GIFT_NOT_FOUND));

        // 1. Tìm bản ghi đổi quà đang PENDING của hội viên
        List<UserGiftRedemptionJpaEntity> pendingGifts = userGiftRedemptionRepository
                .findByTenantIdAndMemberIdAndStatus(tenantId, profile.getId(), RewardConstants.STATUS_PENDING);

        UserGiftRedemptionJpaEntity targetRedemption = pendingGifts.stream()
                .filter(r -> r.getGiftId().equals(gift.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(RewardErrorCode.GIFT_NOT_FOUND));

        // 2. Chuyển trạng thái sang SELECTED và gán vào giỏ
        targetRedemption.setStatus(RewardConstants.STATUS_SELECTED);
        targetRedemption.setAppliedOrderId(request.getCartId());
        userGiftRedemptionRepository.save(targetRedemption);

        cartOfferPort.updateAppliedGift(request.getCartId(), tenantId, gift.getId());

        log.info("Member {} applied gift {} to cart {}", profile.getId(), gift.getTitle(), request.getCartId());

        return CartGiftCalculationResponse.builder()
                .cartId(request.getCartId())
                .selectedGift(CartGiftCalculationResponse.SelectedGiftSummary.builder()
                        .giftId(gift.getId())
                        .title(gift.getTitle())
                        .status(RewardConstants.STATUS_SELECTED)
                        .build())
                .build();
    }

    private void validateCombinationRule(CartOfferPort.CartOfferDetails cart, UUID requestedGiftId) {
        if (cart.appliedGiftId() != null && !cart.appliedGiftId().equals(requestedGiftId)) {
            throw new BusinessException(RewardErrorCode.INVALID_REWARD_COMBINATION);
        }
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
}

