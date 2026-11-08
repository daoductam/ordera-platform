package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.reward.application.port.CartOfferPort;
import com.ordera.modules.reward.application.usecase.ValidatePromoCodeUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.ValidatePromoCodeRequest;
import com.ordera.modules.reward.presentation.dto.ValidatePromoCodeResponse;
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
 * UseCase kiểm tra tính hợp lệ của mã khuyến mãi nhập tay (Figma 08.05, 08.11, 08.12).
 * Kiểm tra min_order_value và tính toán số tiền giảm giá chính xác.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidatePromoCodeUseCaseImpl implements ValidatePromoCodeUseCase {

    private final CartOfferPort cartOfferPort;
    private final VoucherJpaRepository voucherRepository;

    @Override
    @Transactional(readOnly = true)
    public ValidatePromoCodeResponse execute(ValidatePromoCodeRequest request) {
        Objects.requireNonNull(request, "ValidatePromoCodeRequest must not be null");
        UUID tenantId = resolveTenantId();
        resolveUserId(); // Đảm bảo người dùng đã xác thực

        CartOfferPort.CartOfferDetails cart = cartOfferPort.findCartByIdAndTenantId(request.getCartId(), tenantId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.CART_NOT_FOUND));

        VoucherJpaEntity voucher = voucherRepository.findByTenantIdAndCode(tenantId, request.getCode().trim())
                .orElseThrow(() -> new BusinessException(RewardErrorCode.PROMO_CODE_NOT_FOUND));

        validateVoucherActiveAndNotExpired(voucher);
        BigDecimal subtotal = (cart.subtotalAmount() != null) ? cart.subtotalAmount() : BigDecimal.ZERO;
        validateOrderThreshold(subtotal, voucher);

        BigDecimal discountAmount = calculateDiscountAmount(voucher, subtotal);

        return ValidatePromoCodeResponse.builder()
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .discountAmount(discountAmount)
                .minOrderValue(voucher.getMinOrderValue())
                .build();
    }

    private void validateVoucherActiveAndNotExpired(VoucherJpaEntity voucher) {
        if (!voucher.isActive()) {
            throw new BusinessException(RewardErrorCode.PROMO_CODE_NOT_FOUND);
        }
        if (voucher.getValidUntil() != null && voucher.getValidUntil().isBefore(Instant.now())) {
            throw new BusinessException(RewardErrorCode.PROMO_CODE_NOT_FOUND);
        }
    }

    private void validateOrderThreshold(BigDecimal subtotal, VoucherJpaEntity voucher) {
        if (subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            Map<String, Object> details = Map.of(
                    "currentSubtotal", subtotal,
                    "minOrderRequired", voucher.getMinOrderValue()
            );
            throw new BusinessException(RewardErrorCode.PROMO_CODE_INELIGIBLE,
                    RewardErrorCode.PROMO_CODE_INELIGIBLE.getDefaultMessage(), details);
        }
    }

    private BigDecimal calculateDiscountAmount(VoucherJpaEntity voucher, BigDecimal subtotal) {
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
}
