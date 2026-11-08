package com.ordera.modules.reward.domain.exception;

import com.ordera.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Danh mục mã lỗi nghiệp vụ chuẩn hóa cho Phân hệ Ví Voucher & Đổi Thưởng.
 * Tuân thủ RFC 7807 và ErrorCode chuẩn toàn hệ thống Ordera.
 */
@Getter
@RequiredArgsConstructor
public enum RewardErrorCode implements ErrorCode {

    INSUFFICIENT_POINTS("POINTS_INSUFFICIENT", 422, "Số dư điểm khả dụng không đủ để đổi ưu đãi này."),
    GIFT_OUT_OF_STOCK("GIFT_OUT_OF_STOCK", 409, "Rất tiếc! Món quà này đã vừa hết số lượng tồn kho tại hệ thống."),
    PROMO_CODE_NOT_FOUND("PROMO_CODE_NOT_FOUND", 404, "Mã khuyến mãi không hợp lệ hoặc đã hết hạn sử dụng."),
    PROMO_CODE_INELIGIBLE("PROMO_CODE_INELIGIBLE", 422, "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã khuyến mãi này."),
    MINIMUM_ORDER_NOT_MET("MIN_ORDER_NOT_MET", 422, "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã giảm giá này."),
    VOUCHER_ALREADY_USED("VOUCHER_ALREADY_USED", 409, "Voucher này đã được sử dụng hoặc đang áp dụng cho một đơn hàng khác."),
    VOUCHER_EXPIRED("VOUCHER_EXPIRED", 422, "Voucher này đã hết hạn hiệu lực."),
    INVALID_REWARD_COMBINATION("INVALID_REWARD_COMBINATION", 422, "Chỉ cho phép kết hợp tối đa 01 Voucher và 01 Quà hiện vật cho mỗi đơn hàng."),
    VOUCHER_NOT_FOUND("VOUCHER_NOT_FOUND", 404, "Voucher không tồn tại hoặc đã bị gỡ khỏi danh mục đổi thưởng."),
    GIFT_NOT_FOUND("GIFT_NOT_FOUND", 404, "Món quà này không tồn tại hoặc đã ngừng chương trình tặng quà."),
    CONCURRENT_TRANSACTION_LOCK("CONCURRENT_TRANSACTION_LOCK", 409, "Một giao dịch đổi thưởng khác của bạn đang được xử lý. Vui lòng không thao tác đồng thời."),
    TENANT_HEADER_MISSING("TENANT_HEADER_MISSING", 400, "Header 'X-Tenant-ID' is required for multi-tenant data isolation."),
    UNAUTHORIZED("UNAUTHORIZED", 401, "Vui lòng đăng nhập để thực hiện thao tác này."),
    CART_NOT_FOUND("CART_NOT_FOUND", 404, "Giỏ hàng không tồn tại hoặc đã hết hạn phiên làm việc."),
    CART_OR_VOUCHER_NOT_FOUND("CART_OR_VOUCHER_NOT_FOUND", 404, "Giỏ hàng không tồn tại hoặc mã voucher chưa từng được áp dụng vào giỏ hàng này."),
    MEMBER_PROFILE_NOT_FOUND("MEMBER_PROFILE_NOT_FOUND", 404, "Không tìm thấy hồ sơ thành viên tương ứng với tài khoản người dùng.");

    private final String code;
    private final int status;
    private final String defaultMessage;
}

