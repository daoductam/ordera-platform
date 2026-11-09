package com.ordera.modules.order.presentation.constant;

import lombok.experimental.UtilityClass;

/**
 * Thông điệp phản hồi chuẩn hóa cho Phân hệ Đơn hàng, Giỏ hàng, Checkout & Hóa đơn.
 * Tuyệt đối cấm hardcode string phản hồi trong ApiResponse.
 */
@UtilityClass
public class OrderMessages {

    // Thông điệp thành công (MSG_*)
    public static final String MSG_CART_RETRIEVED = "Cart retrieved successfully";
    public static final String MSG_CART_EMPTY = "Cart is currently empty";
    public static final String MSG_ITEM_ADDED_TO_CART = "Item added to cart successfully";
    public static final String MSG_CART_ITEM_UPDATED = "Cart item updated successfully";
    public static final String MSG_CART_ITEM_REMOVED = "Item removed from cart successfully";
    public static final String MSG_ALLERGY_NOTE_UPDATED = "Cart comment updated successfully";
    public static final String MSG_CHECKOUT_PREVIEW_CALCULATED = "Bảng dự toán chi phí đơn hàng";
    public static final String MSG_ORDER_CHECKOUT_SUCCESS = "Đơn hàng của bạn đã được tiếp nhận và thanh toán thành công";
    public static final String MSG_ORDERS_LIST_RETRIEVED = "Customer orders retrieved successfully";
    public static final String MSG_ORDER_DETAIL_RETRIEVED = "Order details retrieved successfully";
    public static final String MSG_REORDER_SUCCESS = "Các món từ đơn cũ đã được tải lại vào giỏ hàng của bạn.";
    public static final String MSG_RECEIPT_EMAIL_SENT = "Email sent. The receipt is sent to the email.";

    // Mô tả lỗi tiêu biểu (ERR_*)
    public static final String ERR_CART_NOT_FOUND = "Giỏ hàng không tồn tại hoặc đã hết hạn.";
    public static final String ERR_CART_EMPTY = "Giỏ hàng hiện tại đang trống.";
    public static final String ERR_ORDER_NOT_FOUND = "Đơn hàng không tồn tại hoặc không thuộc quyền sở hữu.";
    public static final String ERR_IDEMPOTENT_CONFLICT = "Yêu cầu thanh toán đơn hàng này đang được xử lý.";
    public static final String ERR_MIN_ORDER_NOT_MET = "Đơn hàng chưa đạt giá trị tối thiểu của Voucher.";
    public static final String ERR_STOCK_UNAVAILABLE = "Một hoặc nhiều món trong đơn hàng đã hết nguyên liệu.";
}

