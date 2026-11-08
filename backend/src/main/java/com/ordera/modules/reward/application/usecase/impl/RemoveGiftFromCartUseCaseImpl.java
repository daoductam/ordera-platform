package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.port.CartOfferPort;
import com.ordera.modules.reward.application.usecase.RemoveGiftFromCartUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserGiftRedemptionJpaRepository;
import com.ordera.modules.reward.presentation.dto.CartGiftCalculationResponse;
import com.ordera.modules.reward.presentation.dto.RemoveGiftRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Gỡ bỏ quà tặng tri ân khỏi giỏ hàng (Figma 08.09).
 * Hoàn trả trạng thái lượt đổi quà từ SELECTED về PENDING.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveGiftFromCartUseCaseImpl implements RemoveGiftFromCartUseCase {

    private final CartOfferPort cartOfferPort;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final UserGiftRedemptionJpaRepository userGiftRedemptionRepository;

    @Override
    @Transactional
    public CartGiftCalculationResponse execute(RemoveGiftRequest request) {
        Objects.requireNonNull(request, "RemoveGiftRequest must not be null");
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity profile = resolveMemberProfile(tenantId, userId);

        CartOfferPort.CartOfferDetails cart = cartOfferPort.findCartByIdAndTenantId(request.getCartId(), tenantId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.CART_NOT_FOUND));

        if (cart.appliedGiftId() == null || !cart.appliedGiftId().equals(request.getRewardGiftId())) {
            throw new BusinessException(RewardErrorCode.GIFT_NOT_FOUND);
        }

        // 1. Hoàn trả trạng thái lượt đổi quà về PENDING
        Optional<UserGiftRedemptionJpaEntity> redemptionOpt = userGiftRedemptionRepository.findByTenantIdAndAppliedOrderId(
                tenantId, request.getCartId());
        redemptionOpt.ifPresent(r -> {
            r.setStatus(RewardConstants.STATUS_PENDING);
            r.setAppliedOrderId(null);
            userGiftRedemptionRepository.save(r);
        });

        // 2. Gỡ bỏ quà khỏi giỏ hàng
        cartOfferPort.clearAppliedGift(request.getCartId(), tenantId);

        log.info("Member {} removed gift {} from cart {}", profile.getId(), request.getRewardGiftId(), request.getCartId());

        return CartGiftCalculationResponse.builder()
                .cartId(request.getCartId())
                .selectedGift(null)
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
}
