package com.ordera.modules.loyalty.domain.exception;

import com.ordera.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Danh mục mã lỗi nghiệp vụ riêng của Phân hệ Loyalty & Sổ cái Điểm.
 * Tuân thủ RFC 7807 và chuẩn ErrorCode toàn hệ thống.
 * Độc lập hoàn toàn với Spring Framework và JPA.
 */
@Getter
@RequiredArgsConstructor
public enum LoyaltyErrorCode implements ErrorCode {

    MEMBER_PROFILE_NOT_FOUND("MEMBER_PROFILE_NOT_FOUND", 404, "Không tìm thấy hồ sơ thành viên tương ứng với tài khoản người dùng trong thương hiệu này."),
    INVALID_TIER_TRANSITION("INVALID_TIER_TRANSITION", 400, "Bước chuyển hạng thẻ thành viên không hợp lệ."),
    INSUFFICIENT_POINTS("INSUFFICIENT_POINTS", 400, "Số dư điểm thưởng không đủ để thực hiện giao dịch."),
    RECEIPT_EXPIRED("RECEIPT_EXPIRED", 422, "Hệ thống chỉ hỗ trợ tiếp nhận khiếu nại cộng điểm cho hóa đơn phát sinh trong vòng 30 ngày gần nhất."),
    INVALID_RECEIPT_DATE("INVALID_RECEIPT_DATE", 400, "Ngày lập hóa đơn không được vượt quá ngày hiện tại."),
    CLAIM_ALREADY_EXISTS("CLAIM_ALREADY_EXISTS", 409, "Mã hóa đơn này đã được gửi khiếu nại trước đó hoặc đã được cộng điểm trong hệ thống."),
    CLAIM_NOT_FOUND("CLAIM_NOT_FOUND", 404, "Không tìm thấy phiếu khiếu nại thiếu điểm."),
    TENANT_HEADER_MISSING("TENANT_HEADER_MISSING", 400, "Header 'X-Tenant-ID' is required for multi-tenant data isolation."),
    FILE_TOO_LARGE("FILE_TOO_LARGE", 413, "Dung lượng ảnh vượt quá giới hạn tối đa cho phép (5MB)."),
    UNSUPPORTED_IMAGE_FORMAT("UNSUPPORTED_IMAGE_FORMAT", 415, "Chỉ chấp nhận các tệp ảnh định dạng JPEG, PNG hoặc WebP."),
    CONCURRENT_TRANSACTION_LOCK("CONCURRENT_TRANSACTION_LOCK", 409, "Yêu cầu giao dịch điểm đang được xử lý, vui lòng không thao tác đồng thời."),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", 409, "Địa chỉ email này đã được liên kết với một tài khoản thành viên khác trong thương hiệu."),
    MISSING_REQUIRED_FIELDS("MISSING_REQUIRED_FIELDS", 400, "Họ tên và email là thông tin bắt buộc để kích hoạt tài khoản."),
    UNAUTHORIZED("UNAUTHORIZED", 401, "Hội viên chưa xác thực hoặc phiên đăng nhập đã hết hạn.");

    private final String code;
    private final int status;
    private final String defaultMessage;
}

