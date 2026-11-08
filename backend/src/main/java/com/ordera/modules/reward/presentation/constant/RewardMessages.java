package com.ordera.modules.reward.presentation.constant;

import lombok.experimental.UtilityClass;

/**
 * Hằng số chuỗi thông điệp phản hồi API cho Phân hệ Ví Voucher & Đổi Thưởng.
 * Ngăn chặn tuyệt đối việc hardcode chuỗi trong ApiResponse.
 */
@UtilityClass
public class RewardMessages {

    public static final String MSG_USER_VOUCHERS_RETRIEVED = "My rewards retrieved successfully";
    public static final String MSG_EMPTY_WALLET = "Không có ưu đãi nào trong ví";
    public static final String MSG_REWARD_CATALOG_RETRIEVED = "Rewards catalog retrieved successfully";
    public static final String MSG_VOUCHER_REDEEMED = "Đổi Voucher thành công! Mã đã được lưu vào ví My Rewards.";
    public static final String MSG_GIFT_REDEEMED = "Đổi quà thành công! Hãy xuất trình mã này cho nhân viên quầy thu ngân để nhận quà.";
    public static final String MSG_OFFERS_SUMMARY_RETRIEVED = "Rewards summary retrieved";
    public static final String MSG_ELIGIBLE_OFFERS_RETRIEVED = "Available offers evaluated successfully";
    public static final String MSG_PROMO_CODE_VALIDATED = "Mã khuyến mãi hợp lệ";
    public static final String MSG_VOUCHER_APPLIED = "Áp dụng voucher thành công";
    public static final String MSG_VOUCHER_REMOVED = "Đã gỡ bỏ voucher khỏi giỏ hàng";
    public static final String MSG_GIFT_APPLIED = "Quà tặng tri ân đã được thêm vào đơn hàng của bạn";
    public static final String MSG_GIFT_REMOVED = "Đã gỡ quà tặng khỏi đơn hàng";

    // Error messages
    public static final String ERR_TENANT_HEADER_MISSING = "Header 'X-Tenant-ID' is required for multi-tenant data isolation.";
    public static final String ERR_UNAUTHORIZED = "Vui lòng đăng nhập để thực hiện thao tác.";
    public static final String ERR_CART_NOT_FOUND = "Giỏ hàng không tồn tại hoặc đã hết hạn phiên làm việc.";
}

