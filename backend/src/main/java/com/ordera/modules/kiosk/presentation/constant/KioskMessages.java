package com.ordera.modules.kiosk.presentation.constant;

public final class KioskMessages {

    private KioskMessages() {
        // Prevent instantiation
    }

    // Success Messages
    public static final String MSG_CUSTOMER_IDENTIFIED = "Customer identified successfully";
    public static final String MSG_CUSTOMER_NOT_REGISTERED = "Customer not registered, temporary session created";
    public static final String MSG_QUICK_POINTS_EARNED = "Points credited successfully";
    public static final String MSG_KIOSK_ORDER_PLACED = "Kiosk order placed successfully";
    public static final String MSG_SESSION_RESET = "Kiosk session cleared and reset to standby";
    public static final String MSG_KIOSK_CONFIG_RETRIEVED = "Kiosk configuration retrieved";

    // Error Descriptions
    public static final String ERR_TENANT_HEADER_MISSING = "Header 'X-Tenant-ID' is required for multi-tenant data isolation.";
    public static final String ERR_DEVICE_HEADER_MISSING = "Header 'X-Kiosk-Device-ID' is required to verify kiosk terminal.";
    public static final String ERR_INVALID_PHONE_NUMBER = "Số điện thoại phải có ít nhất 8 chữ số và đúng định dạng quốc tế.";
    public static final String ERR_KIOSK_DEVICE_NOT_FOUND = "Thiết bị Kiosk không tồn tại hoặc chưa được đăng ký trong hệ thống chi nhánh.";
    public static final String ERR_INVALID_STAFF_PIN = "Mã PIN nhân viên thu ngân không chính xác.";
    public static final String ERR_INVALID_BILL_AMOUNT = "Số tiền hóa đơn tích điểm tối thiểu phải từ 1.00 NOK trở lên.";
    public static final String ERR_PRODUCT_NOT_FOUND = "Một hoặc nhiều món ăn đã chọn không còn tồn tại hoặc tạm ngưng phục vụ.";
    public static final String ERR_CARD_READER_ERROR = "Giao dịch thanh toán thẻ không thành công hoặc bị từ chối bởi ngân hàng.";
}

