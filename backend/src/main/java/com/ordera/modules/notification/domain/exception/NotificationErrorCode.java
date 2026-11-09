package com.ordera.modules.notification.domain.exception;

import com.ordera.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Danh mục mã lỗi nghiệp vụ chuẩn hóa cho Phân hệ Thông Báo & Hỗ Trợ Khách Hàng.
 * Tuân thủ RFC 7807 Problem Details và interface ErrorCode của Ordera Platform.
 */
@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_NOT_FOUND("NOTIFICATION_NOT_FOUND", 404, "Không tìm thấy thông báo với ID được chỉ định."),
    INQUIRY_NOT_FOUND("INQUIRY_NOT_FOUND", 404, "Không tìm thấy phiếu yêu cầu tra cứu điểm."),
    INQUIRY_VALIDATION_FAILED("INQUIRY_VALIDATION_FAILED", 422, "Thông tin phiếu khiếu nại không hợp lệ hoặc vượt quá giới hạn."),
    UNAUTHORIZED("UNAUTHORIZED", 401, "Vui lòng đăng nhập để thực hiện thao tác này."),
    TENANT_HEADER_MISSING("TENANT_HEADER_MISSING", 400, "Header 'X-Tenant-ID' is required for multi-tenant data isolation.");

    private final String code;
    private final int status;
    private final String defaultMessage;
}

