package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.port.CartOfferPort;
import com.ordera.modules.reward.application.usecase.ApplyVoucherToCartUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.ApplyVoucherRequest;
import com.ordera.modules.reward.presentation.dto.CartVoucherCalculationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Áp dụng mã voucher vào giỏ hàng (Figma 08.06, 08.07).
 * Khóa voucher sang IN_USE, thực thi quy tắc tối đa 01 voucher/đơn hàng và tính toán lại chi phí.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyVoucherToCartUseCaseImpl implements ApplyVoucherToCartUseCase {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.15");

    private final CartOfferPort cartOfferPort;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final VoucherJpaRepository voucherRepository;
    private final UserVoucherJpaRepository userVoucherRepository;

    @Override
    @Transactional
    public CartVoucherCalculationResponse execute(ApplyVoucherRequest request) {
        Objects.requireNonNull(request, "ApplyVoucherRequest must not be null");
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity profile = resolveMemberProfile(tenantId, userId);

        CartOfferPort.CartOfferDetails cart = cartOfferPort.findCartByIdAndTenantId(request.getCartId(), tenantId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.CART_NOT_FOUND));

        validateCombinationRule(cart, request.getVoucherCode());

        VoucherJpaEntity voucher = voucherRepository.findByTenantIdAndCode(tenantId, request.getVoucherCode().trim())
                .orElseThrow(() -> new BusinessException(RewardErrorCode.VOUCHER_NOT_FOUND));
        validateVoucherActiveAndNotExpired(voucher);

        BigDecimal subtotal = (cart.subtotalAmount() != null) ? cart.subtotalAmount() : BigDecimal.ZERO;
        validateOrderThreshold(subtotal, voucher);

        UserVoucherJpaEntity userVoucher = userVoucherRepository.findByTenantIdAndMemberIdAndVoucherIdAndStatus(
                tenantId, profile.getId(), voucher.getId(), RewardConstants.STATUS_AVAILABLE)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.VOUCHER_NOT_FOUND));

        // 1. Chuyển trạng thái voucher sang IN_USE và gán applied_order_id
        userVoucher.setStatus(RewardConstants.STATUS_IN_USE);
        userVoucher.setAppliedOrderId(request.getCartId());
        userVoucherRepository.save(userVoucher);

        // 2. Cập nhật mã voucher vào giỏ hàng
        cartOfferPort.updateAppliedVoucher(request.getCartId(), tenantId, voucher.getCode());

        BigDecimal discountAmount = calculateDiscount(voucher, subtotal);
        BigDecimal netAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);
        BigDecimal vatAmount = netAmount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = netAmount;

        return CartVoucherCalculationResponse.builder()
                .cartId(request.getCartId())
                .appliedVoucher(CartVoucherCalculationResponse.AppliedVoucherSummary.builder()
                        .code(voucher.getCode())
                        .title(voucher.getTitle())
                        .discountAmount(discountAmount)
                        .build())
                .subtotalAmount(subtotal)
                .discountAmount(discountAmount)
                .vatAmount(vatAmount)
                .finalTotalAmount(finalTotal)
                .build();
    }

    private void validateCombinationRule(CartOfferPort.CartOfferDetails cart, String requestedCode) {
        if (cart.appliedVoucherCode() != null && !cart.appliedVoucherCode().isBlank()) {
            if (!cart.appliedVoucherCode().equalsIgnoreCase(requestedCode.trim())) {
                throw new BusinessException(RewardErrorCode.INVALID_REWARD_COMBINATION);
            }
        }
    }

    private void validateVoucherActiveAndNotExpired(VoucherJpaEntity voucher) {
        if (!voucher.isActive()) {
            throw new BusinessException(RewardErrorCode.VOUCHER_NOT_FOUND);
        }
        if (voucher.getValidUntil() != null && voucher.getValidUntil().isBefore(Instant.now())) {
            throw new BusinessException(RewardErrorCode.VOUCHER_EXPIRED);
        }
    }

    private void validateOrderThreshold(BigDecimal subtotal, VoucherJpaEntity voucher) {
        if (subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            Map<String, Object> details = Map.of(
                    "currentSubtotal", subtotal,
                    "minOrderRequired", voucher.getMinOrderValue()
            );
            throw new BusinessException(RewardErrorCode.MINIMUM_ORDER_NOT_MET,
                    RewardErrorCode.MINIMUM_ORDER_NOT_MET.getDefaultMessage(), details);
        }
    }

    private BigDecimal calculateDiscount(VoucherJpaEntity voucher, BigDecimal subtotal) {
        if (RewardConstants.DISCOUNT_TYPE_PERCENT.equalsIgnoreCase(voucher.getDiscountType())) {
            BigDecimal rate = voucher.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal discount = subtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            return discount.min(subtotal);
        }
        return voucher.getDiscountValue().min(subtotal);
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
