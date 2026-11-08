package com.ordera.modules.reward.application.port;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng giao tiếp ngoại vi (Port) cho phép phân hệ Reward tương tác với Giỏ hàng (Cart).
 * Đảm bảo Clean Architecture và Dependency Inversion giữa Reward và Order modules.
 */
public interface CartOfferPort {

    record CartOfferDetails(
            UUID cartId,
            UUID tenantId,
            UUID storeId,
            UUID userId,
            BigDecimal subtotalAmount,
            String appliedVoucherCode,
            UUID appliedGiftId
    ) {}

    /**
     * Lấy thông tin giỏ hàng và tổng tiền subtotal hiện tại.
     */
    Optional<CartOfferDetails> findCartByIdAndTenantId(UUID cartId, UUID tenantId);

    /**
     * Áp dụng mã voucher vào giỏ hàng.
     */
    void updateAppliedVoucher(UUID cartId, UUID tenantId, String voucherCode);

    /**
     * Gỡ bỏ mã voucher khỏi giỏ hàng.
     */
    void clearAppliedVoucher(UUID cartId, UUID tenantId);

    /**
     * Gán quà tặng tri ân vào giỏ hàng.
     */
    void updateAppliedGift(UUID cartId, UUID tenantId, UUID giftId);

    /**
     * Gỡ bỏ quà tặng tri ân khỏi giỏ hàng.
     */
    void clearAppliedGift(UUID cartId, UUID tenantId);
}

