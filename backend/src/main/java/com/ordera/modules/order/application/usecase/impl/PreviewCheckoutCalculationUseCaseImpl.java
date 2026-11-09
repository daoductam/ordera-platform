package com.ordera.modules.order.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.OrderCalculationUtils;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.order.application.service.CartService;
import com.ordera.modules.order.application.usecase.PreviewCheckoutCalculationUseCase;
import com.ordera.modules.order.domain.constant.OrderConstants;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.domain.model.Cart;
import com.ordera.modules.order.presentation.dto.request.PreviewCheckoutRequest;
import com.ordera.modules.order.presentation.dto.response.AppliedVoucherResponse;
import com.ordera.modules.order.presentation.dto.response.PreviewCheckoutResponse;
import com.ordera.modules.order.presentation.dto.response.SelectedGiftResponse;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

/**
 * Use Case dự toán bảng tính Checkout (API 6.5).
 * Tính toán chính xác Subtotal, Discount, Tip, VAT 15%, Total và số điểm thưởng dự kiến.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreviewCheckoutCalculationUseCaseImpl implements PreviewCheckoutCalculationUseCase {

    private final CartService cartService;
    private final VoucherJpaRepository voucherRepository;
    private final LoyaltyGiftJpaRepository loyaltyGiftRepository;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final TierDefinitionJpaRepository tierDefinitionRepository;

    @Override
    @Transactional(readOnly = true)
    public PreviewCheckoutResponse execute(UUID tenantId, UUID userId, PreviewCheckoutRequest request) {
        Cart cart = cartService.getCartDomain(tenantId, request.getCartId());
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException(OrderErrorCode.CART_EMPTY);
        }

        BigDecimal subtotal = cart.getSubtotalAmount() != null ? cart.getSubtotalAmount() : BigDecimal.ZERO;
        BigDecimal safeTip = request.getTipAmount() != null ? request.getTipAmount() : BigDecimal.ZERO;

        AppliedVoucherResponse appliedVoucher = evaluateVoucher(tenantId, request.getVoucherCode(), subtotal);
        BigDecimal discount = appliedVoucher != null ? appliedVoucher.getDiscountAmount() : BigDecimal.ZERO;

        SelectedGiftResponse selectedGift = evaluateGift(tenantId, request.getRewardGiftId());

        BigDecimal total = OrderCalculationUtils.calculateTotal(subtotal, discount, safeTip);
        BigDecimal vat = OrderCalculationUtils.calculateVat(total);

        BigDecimal tierRate = resolveTierRate(tenantId, userId);
        int pointsToEarn = OrderCalculationUtils.calculatePointsToEarn(subtotal, discount, tierRate);

        return PreviewCheckoutResponse.builder()
                .subtotalAmount(subtotal.setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP))
                .discountAmount(discount.setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP))
                .tipAmount(safeTip.setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP))
                .vatAmount(vat.setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP))
                .vatPercentage(15.0)
                .finalTotalAmount(total.setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP))
                .currency(OrderConstants.CURRENCY_NOK)
                .pointsToEarn(pointsToEarn)
                .appliedVoucher(appliedVoucher)
                .selectedGift(selectedGift)
                .build();
    }

    private AppliedVoucherResponse evaluateVoucher(UUID tenantId, String voucherCode, BigDecimal subtotal) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return null;
        }

        VoucherJpaEntity voucher = voucherRepository.findByTenantIdAndCode(tenantId, voucherCode.trim())
                .orElse(null);

        if (voucher == null) {
            return null;
        }

        if (voucher.getMinOrderValue() != null && subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new BusinessException(OrderErrorCode.MINIMUM_ORDER_VALUE_NOT_MET,
                    String.format("Đơn hàng chưa đạt giá trị tối thiểu %.2f NOK của Voucher (Hiện tại: %.2f NOK).",
                            voucher.getMinOrderValue(), subtotal));
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        String discountText = "";

        if (RewardConstants.DISCOUNT_TYPE_PERCENT.equalsIgnoreCase(voucher.getDiscountType())) {
            BigDecimal percent = voucher.getDiscountValue() != null ? voucher.getDiscountValue() : BigDecimal.ZERO;
            discountAmount = subtotal.multiply(percent).divide(new BigDecimal("100"), OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP);
            discountText = "-" + percent.stripTrailingZeros().toPlainString() + " %";
        } else {
            BigDecimal fixed = voucher.getDiscountValue() != null ? voucher.getDiscountValue() : BigDecimal.ZERO;
            discountAmount = fixed.min(subtotal);
            discountText = "-" + fixed.stripTrailingZeros().toPlainString() + " NOK";
        }

        return AppliedVoucherResponse.builder()
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .discountValueText(discountText)
                .discountAmount(discountAmount)
                .build();
    }

    private SelectedGiftResponse evaluateGift(UUID tenantId, UUID giftId) {
        if (giftId == null) {
            return null;
        }
        Optional<LoyaltyGiftJpaEntity> giftOpt = loyaltyGiftRepository.findByIdAndTenantId(giftId, tenantId);
        return giftOpt.map(gift -> SelectedGiftResponse.builder()
                .giftId(gift.getId())
                .title(gift.getTitle())
                .build()).orElse(null);
    }

    private BigDecimal resolveTierRate(UUID tenantId, UUID userId) {
        if (userId == null) {
            return BigDecimal.ONE;
        }
        Optional<MemberProfileJpaEntity> profileOpt = memberProfileRepository.findByTenantIdAndUserId(tenantId, userId);
        if (profileOpt.isEmpty() || profileOpt.get().getCurrentTierId() == null) {
            return BigDecimal.ONE;
        }
        Optional<TierDefinitionJpaEntity> tierOpt = tierDefinitionRepository.findById(profileOpt.get().getCurrentTierId());
        return tierOpt.map(TierDefinitionJpaEntity::getEarnPointsRate).orElse(BigDecimal.ONE);
    }
}
