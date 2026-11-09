package com.ordera.modules.order.domain.exception;

import com.ordera.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Danh mục mã lỗi nghiệp vụ chuẩn hóa cho Phân hệ Đơn hàng & Checkout.
 * Tuân thủ RFC 7807 và ErrorCode chuẩn toàn hệ thống Ordera.
 */
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    CART_EMPTY("CART_EMPTY", 404, "Giỏ hàng hiện tại đang trống, không thể tiến hành đặt đơn."),
    MINIMUM_ORDER_VALUE_NOT_MET("MIN_ORDER_VALUE_NOT_MET", 422, "Đơn hàng chưa đạt giá trị tối thiểu của Voucher để áp dụng giảm trừ."),
    IDEMPOTENT_OPERATION_CONFLICT("IDEMPOTENT_OPERATION_CONFLICT", 409, "Yêu cầu thanh toán đơn hàng này đang được xử lý hoặc đã hoàn tất, vui lòng không gửi lại."),
    STOCK_UNAVAILABLE("STOCK_UNAVAILABLE", 409, "Một hoặc nhiều món trong đơn hàng đã hết nguyên liệu phục vụ hoặc ngừng kinh doanh."),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", 404, "Đơn hàng không tồn tại hoặc không thuộc quyền sở hữu của bạn."),
    INVALID_ORDER_STATUS("INVALID_ORDER_STATUS", 422, "Trạng thái đơn hàng không hợp lệ cho thao tác này."),
    CART_NOT_FOUND("CART_NOT_FOUND", 404, "Giỏ hàng không tồn tại hoặc đã hết hạn phiên làm việc."),
    CART_ITEM_NOT_FOUND("CART_ITEM_NOT_FOUND", 404, "Món ăn này không còn tồn tại trong giỏ hàng."),
    INVALID_QUANTITY("INVALID_QUANTITY", 422, "Số lượng món ăn phải nằm trong khoảng từ 1 đến 99."),
    COMMENT_TOO_LONG("COMMENT_TOO_LONG", 422, "Ghi chú đơn hàng không được vượt quá 200 ký tự."),
    INVALID_PICKUP_DETAILS("INVALID_PICKUP_DETAILS", 400, "Vui lòng nhập đầy đủ thông tin bàn ăn hoặc số điện thoại người nhận."),
    TENANT_HEADER_MISSING("TENANT_HEADER_MISSING", 400, "Header 'X-Tenant-ID' is required for multi-tenant data isolation."),
    UNAUTHORIZED("UNAUTHORIZED", 401, "Vui lòng đăng nhập để thực hiện thao tác này."),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", 404, "Món ăn không tồn tại trong thực đơn hoặc hiện đang tạm ngưng phục vụ."),
    CONCURRENT_CHECKOUT_LOCK("CONCURRENT_CHECKOUT_LOCK", 409, "Đơn hàng của bạn đang được xử lý, vui lòng không nhấn thanh toán liên tục."),
    RECEIPT_EMAIL_DISPATCH_FAILED("RECEIPT_EMAIL_DISPATCH_FAILED", 500, "Không thể gửi email hóa đơn điện tử lúc này. Vui lòng thử lại sau.");

    private final String code;
    private final int status;
    private final String defaultMessage;
}
