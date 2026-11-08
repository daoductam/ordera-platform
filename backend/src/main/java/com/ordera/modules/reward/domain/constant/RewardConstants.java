package com.ordera.modules.reward.domain.constant;

import lombok.experimental.UtilityClass;

/**
 * Hằng số tập trung cho Phân hệ Ví Voucher, Đổi Thưởng & Ưu Đãi Giỏ Hàng.
 * Ngăn chặn 100% hiện tượng Magic Numbers & Magic Strings.
 */
@UtilityClass
public class RewardConstants {

    // Quy tắc nghiệp vụ ưu đãi đơn hàng
    public static final int MAX_VOUCHERS_PER_ORDER = 1;
    public static final int MAX_PHYSICAL_GIFTS_PER_ORDER = 1;
    public static final int GIFT_REDEMPTION_CODE_LENGTH = 8;
    public static final int PROMO_CODE_MIN_LENGTH = 3;
    public static final int PROMO_CODE_MAX_LENGTH = 20;

    // Cấu hình Redisson Distributed Lock
    public static final int LOCK_ACQUIRE_TIMEOUT_SEC = 3;
    public static final int LOCK_LEASE_TIMEOUT_SEC = 5;
    public static final String LOCK_KEY_REWARD_PATTERN = "lock:tenant:%s:reward:%s";
    public static final String LOCK_KEY_MEMBER_PATTERN = "lock:tenant:%s:reward:member:%s";
    public static final String LOCK_KEY_GIFT_PATTERN = "lock:tenant:%s:reward:gift:%s";

    // Trạng thái Voucher trong ví hội viên (user_vouchers.status)
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_IN_USE = "IN_USE";
    public static final String STATUS_USED = "USED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    // Trạng thái Lượt đổi quà hiện vật (user_gift_redemptions.status)
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SELECTED = "SELECTED";
    public static final String STATUS_CLAIMED = "CLAIMED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Loại giảm giá (vouchers.discount_type)
    public static final String DISCOUNT_TYPE_FIXED = "FIXED_AMOUNT";
    public static final String DISCOUNT_TYPE_PERCENT = "PERCENTAGE";

    // Bộ lọc loại phần thưởng
    public static final String TYPE_ALL = "ALL";
    public static final String TYPE_VOUCHER = "VOUCHER";
    public static final String TYPE_GIFT = "GIFT";

    // Định dạng tiền tệ hiển thị
    public static final String CURRENCY_NOK = "NOK";
    public static final String PERCENT_SYMBOL = "%";
}
