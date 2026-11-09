package com.ordera.modules.kiosk.domain.exception;

import com.ordera.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Danh mục mã lỗi nghiệp vụ chuẩn hóa cho Phân hệ Kiosk POS & Tích Điểm Nhanh Tại Quầy.
 * Tuân thủ RFC 7807 Problem Details và interface ErrorCode của Ordera Platform.
 */
@Getter
@RequiredArgsConstructor
public enum KioskErrorCode implements ErrorCode {

    INVALID_PHONE_NUMBER("INVALID_PHONE_NUMBER", 400, "Số điện thoại phải có ít nhất 8 chữ số và đúng định dạng quốc tế."),
    KIOSK_DEVICE_NOT_FOUND("KIOSK_DEVICE_NOT_FOUND", 404, "Thiết bị Kiosk không tồn tại hoặc chưa được đăng ký trong hệ thống chi nhánh."),
    INVALID_STAFF_PIN("INVALID_STAFF_PIN", 401, "Mã PIN nhân viên thu ngân không chính xác."),
    KIOSK_SESSION_EXPIRED("KIOSK_SESSION_EXPIRED", 400, "Phiên làm việc Kiosk đã hết hạn do quá thời gian chờ."),
    INVALID_BILL_AMOUNT("INVALID_BILL_AMOUNT", 422, "Số tiền hóa đơn tích điểm tối thiểu phải từ 1.00 NOK trở lên."),
    TENANT_HEADER_MISSING("TENANT_HEADER_MISSING", 400, "Header 'X-Tenant-ID' is required for multi-tenant data isolation."),
    DEVICE_HEADER_MISSING("DEVICE_HEADER_MISSING", 400, "Header 'X-Kiosk-Device-ID' is required to verify kiosk terminal."),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", 404, "Một hoặc nhiều món ăn đã chọn không còn tồn tại hoặc tạm ngưng phục vụ."),
    CARD_READER_ERROR("CARD_READER_ERROR", 422, "Giao dịch thanh toán thẻ không thành công hoặc bị từ chối bởi ngân hàng."),
    CONCURRENT_KIOSK_LOCK("CONCURRENT_KIOSK_LOCK", 409, "Thao tác trên Kiosk đang được xử lý đồng thời, vui lòng thử lại sau giây lát.");

    private final String code;
    private final int status;
    private final String defaultMessage;
}

