package com.ordera.modules.order.domain.constant;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

/**
 * Hằng số tập trung cho Phân hệ Đơn hàng, Giỏ hàng, Checkout & Hóa đơn E-Receipt.
 * Cấm tuyệt đối magic numbers/strings trong mã nguồn.
 */
@UtilityClass
public class OrderConstants {

    // Quy tắc tính toán tài chính & thuế VAT 15%
    public static final BigDecimal VAT_RATE = new BigDecimal("0.15");
    public static final BigDecimal VAT_DIVISOR = new BigDecimal("1.15");
    public static final double VAT_PERCENT_DISPLAY = 15.0;
    public static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");
    public static final int DECIMAL_SCALE = 2;

    // Vòng đời lưu trữ Cache & Idempotency
    public static final int CART_TTL_DAYS = 7;
    public static final int IDEMPOTENCY_KEY_TTL_HOURS = 24;
    public static final String REDIS_CART_PREFIX = "cart:";
    public static final String REDIS_IDEMPOTENCY_PREFIX = "idempotency:order:";

    // Giới hạn ghi chú đơn hàng
    public static final int MAX_ALLERGY_NOTE_LENGTH = 200;

    // Định dạng tiền tố mã đơn & số thứ tự nhận món
    public static final String ORDER_CODE_PREFIX = "ORD-";
    public static final String QUEUE_NUMBER_PREFIX = "#";
    public static final String CURRENCY_NOK = "NOK";

    // Cấu hình Redisson Concurrency Lock
    public static final String LOCK_KEY_CHECKOUT_PATTERN = "lock:order:checkout:%s";
    public static final int REDISSON_LOCK_ACQUIRE_TIMEOUT_SEC = 5;
    public static final int REDISSON_LOCK_LEASE_TIMEOUT_SEC = 10;

    // Thời gian ước tính nhận món mặc định (phút)
    public static final int DEFAULT_ESTIMATED_READY_MINUTES = 10;

    // Bộ lọc danh sách đơn hàng
    public static final String TAB_ACTIVE = "ACTIVE";
    public static final String TAB_PAST = "PAST";

    // Các bước tiến trình chế biến món ăn tại bếp
    public static final String TIMELINE_PLACED = "PLACED";
    public static final String TIMELINE_IN_KITCHEN = "IN_KITCHEN";
    public static final String TIMELINE_READY_FOR_PICKUP = "READY_FOR_PICKUP";
}

