package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.port.CartOfferPort;
import com.ordera.modules.reward.application.usecase.GetAvailableOffersForCartUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.mapper.RewardDtoMapper;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.AvailableOffersResponse;
import com.ordera.modules.reward.presentation.dto.CartOfferGiftItemResponse;
import com.ordera.modules.reward.presentation.dto.CartOfferVoucherItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UseCase lấy danh sách ưu đãi đủ điều kiện áp dụng cho đơn hàng hiện tại (Figma 08.02, 08.04).
 * Đánh giá subtotalAmount với minOrderValue của từng voucher trong ví thành viên.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetAvailableOffersForCartUseCaseImpl implements GetAvailableOffersForCartUseCase {

    private final CartOfferPort cartOfferPort;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final UserVoucherJpaRepository userVoucherRepository;
    private final VoucherJpaRepository voucherRepository;
    private final LoyaltyGiftJpaRepository loyaltyGiftRepository;
    private final RewardDtoMapper rewardDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public AvailableOffersResponse execute(UUID cartId) {
        Objects.requireNonNull(cartId, "cartId must not be null");
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity profile = resolveMemberProfile(tenantId, userId);

        CartOfferPort.CartOfferDetails cart = cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.CART_NOT_FOUND));

        BigDecimal subtotal = (cart.subtotalAmount() != null) ? cart.subtotalAmount() : BigDecimal.ZERO;
        List<CartOfferVoucherItemResponse> voucherOffers = evaluateVoucherOffers(tenantId, profile.getId(), subtotal);
        List<CartOfferGiftItemResponse> giftOffers = evaluateGiftOffers(tenantId);

        return AvailableOffersResponse.builder()
                .subtotalAmount(subtotal)
                .vouchers(voucherOffers)
                .gifts(giftOffers)
                .build();
    }

    private List<CartOfferVoucherItemResponse> evaluateVoucherOffers(UUID tenantId, UUID memberId, BigDecimal subtotal) {
        List<UserVoucherJpaEntity> userVouchers = userVoucherRepository.findByTenantIdAndMemberId(tenantId, memberId);
        if (userVouchers.isEmpty()) {
            return List.of();
        }

        List<UUID> voucherIds = userVouchers.stream().map(UserVoucherJpaEntity::getVoucherId).toList();
        Map<UUID, VoucherJpaEntity> voucherMap = voucherRepository.findAllById(voucherIds).stream()
                .collect(Collectors.toMap(VoucherJpaEntity::getId, v -> v));

        List<CartOfferVoucherItemResponse> result = new ArrayList<>();
        for (UserVoucherJpaEntity uv : userVouchers) {
            VoucherJpaEntity v = voucherMap.get(uv.getVoucherId());
            if (v == null || !v.isActive()) {
                continue;
            }

            CartOfferVoucherItemResponse item = rewardDtoMapper.toCartOfferVoucherItem(v);
            item.setStatus(uv.getStatus());

            boolean isApplicable = subtotal.compareTo(v.getMinOrderValue()) >= 0;
            item.setApplicable(isApplicable);
            if (!isApplicable) {
                item.setIneligibilityReason(String.format("Đơn hàng tối thiểu phải từ %s NOK (Hiện tại: %s NOK)",
                        v.getMinOrderValue().stripTrailingZeros().toPlainString(),
                        subtotal.stripTrailingZeros().toPlainString()));
            }
            result.add(item);
        }
        return result;
    }

    private List<CartOfferGiftItemResponse> evaluateGiftOffers(UUID tenantId) {
        List<LoyaltyGiftJpaEntity> gifts = loyaltyGiftRepository.findByTenantIdAndActive(tenantId, true);
        return gifts.stream()
                .map(rewardDtoMapper::toCartOfferGiftItem)
                .toList();
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
