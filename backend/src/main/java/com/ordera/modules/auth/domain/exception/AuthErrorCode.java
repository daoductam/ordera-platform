package com.ordera.modules.auth.domain.exception;

import com.ordera.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Danh mục mã lỗi chuẩn hóa cho Phân hệ Xác thực & Quản lý Danh tính (Auth & Identity).
 * Độc lập với Spring Framework để sử dụng an toàn trong tầng Domain và ném qua BusinessException.
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    TENANT_HEADER_MISSING("TENANT_HEADER_MISSING", 400, "Required request header 'X-Tenant-ID' is missing."),
    OTP_INVALID("OTP_INVALID", 400, "Mã xác thực OTP không chính xác. Vui lòng thử lại."),
    OTP_EXPIRED("OTP_EXPIRED", 400, "Mã xác thực OTP đã hết hạn hiệu lực. Vui lòng bấm gửi lại mã mới."),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", 401, "Thông tin xác thực không chính xác."),
    TOKEN_EXPIRED("TOKEN_EXPIRED", 401, "Phiên xác thực đã hết hạn hiệu lực. Vui lòng đăng nhập lại."),
    TOKEN_REVOKED("TOKEN_REVOKED", 401, "Token đã bị thu hồi hoặc đã đăng xuất khỏi hệ thống."),
    REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", 401, "Phiên làm việc dài hạn đã hết hạn. Vui lòng đăng nhập lại."),
    USER_NOT_FOUND("USER_NOT_FOUND", 404, "Không tìm thấy thông tin tài khoản người dùng tương ứng."),
    PHONE_ALREADY_EXISTS("PHONE_ALREADY_EXISTS", 409, "Số điện thoại này đã được đăng ký tài khoản tại thương hiệu."),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", 409, "Số điện thoại hoặc Email này đã được đăng ký tài khoản tại thương hiệu."),
    VALIDATION_FAILED("VALIDATION_FAILED", 422, "Dữ liệu đầu vào không hợp lệ."),
    OTP_COOLDOWN_ACTIVE("OTP_COOLDOWN_ACTIVE", 429, "Vui lòng đợi hết thời gian chờ 120 giây trước khi yêu cầu gửi lại mã mới."),
    OTP_MAX_ATTEMPTS_EXCEEDED("OTP_MAX_ATTEMPTS_EXCEEDED", 429, "Bạn đã nhập sai mã xác thực quá 5 lần. Phiên xác thực này đã bị hủy vì lý do bảo mật.");

    private final String code;
    private final int status;
    private final String defaultMessage;
}
