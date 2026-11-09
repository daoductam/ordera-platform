package com.ordera.modules.order.domain;

import com.ordera.modules.order.domain.constant.OrderConstants;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Constants and Error Codes Unit Tests")
class OrderConstantsAndErrorCodesTest {

    @Test
    @DisplayName("should_verify_all_required_order_constants_values")
    void should_verify_all_required_order_constants_values() {
        assertEquals(new BigDecimal("0.15"), OrderConstants.VAT_RATE);
        assertEquals(new BigDecimal("1.15"), OrderConstants.VAT_DIVISOR);
        assertEquals(7, OrderConstants.CART_TTL_DAYS);
        assertEquals(24, OrderConstants.IDEMPOTENCY_KEY_TTL_HOURS);
        assertEquals(200, OrderConstants.MAX_ALLERGY_NOTE_LENGTH);
        assertEquals("ORD-", OrderConstants.ORDER_CODE_PREFIX);
        assertEquals("#", OrderConstants.QUEUE_NUMBER_PREFIX);
        assertEquals(2, OrderConstants.DECIMAL_SCALE);
        assertEquals("lock:order:checkout:%s", OrderConstants.LOCK_KEY_CHECKOUT_PATTERN);
        assertEquals(5, OrderConstants.REDISSON_LOCK_ACQUIRE_TIMEOUT_SEC);
        assertEquals(10, OrderConstants.REDISSON_LOCK_LEASE_TIMEOUT_SEC);
        assertEquals("NOK", OrderConstants.CURRENCY_NOK);
    }

    @Test
    @DisplayName("should_verify_required_order_error_codes_and_status")
    void should_verify_required_order_error_codes_and_status() {
        assertEquals("CART_EMPTY", OrderErrorCode.CART_EMPTY.getCode());
        assertEquals(404, OrderErrorCode.CART_EMPTY.getStatus());

        assertEquals("MIN_ORDER_VALUE_NOT_MET", OrderErrorCode.MINIMUM_ORDER_VALUE_NOT_MET.getCode());
        assertEquals(422, OrderErrorCode.MINIMUM_ORDER_VALUE_NOT_MET.getStatus());

        assertEquals("IDEMPOTENT_OPERATION_CONFLICT", OrderErrorCode.IDEMPOTENT_OPERATION_CONFLICT.getCode());
        assertEquals(409, OrderErrorCode.IDEMPOTENT_OPERATION_CONFLICT.getStatus());

        assertEquals("STOCK_UNAVAILABLE", OrderErrorCode.STOCK_UNAVAILABLE.getCode());
        assertEquals(409, OrderErrorCode.STOCK_UNAVAILABLE.getStatus());

        assertEquals("ORDER_NOT_FOUND", OrderErrorCode.ORDER_NOT_FOUND.getCode());
        assertEquals(404, OrderErrorCode.ORDER_NOT_FOUND.getStatus());

        assertEquals("INVALID_ORDER_STATUS", OrderErrorCode.INVALID_ORDER_STATUS.getCode());
        assertEquals(422, OrderErrorCode.INVALID_ORDER_STATUS.getStatus());
    }
}
