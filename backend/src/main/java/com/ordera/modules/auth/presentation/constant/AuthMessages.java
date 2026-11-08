package com.ordera.modules.auth.presentation.constant;

/**
 * Tập hợp các thông điệp phản hồi API chuẩn hóa cho tầng Presentation của Phân hệ Auth & Identity.
 * Đặt trong tầng Presentation (presentation/constant) để tách biệt hoàn toàn với tầng Domain.
 */
public final class AuthMessages {

    private AuthMessages() {
        // Ngăn chặn khởi tạo đối tượng tiện ích thông điệp
    }

    public static final String MSG_OTP_SENT_SMS = "Mã xác thực OTP đã được gửi thành công qua tin nhắn SMS.";
    public static final String MSG_LOGIN_COMPLETE = "Xác thực mã OTP thành công. Đăng nhập hoàn tất.";
    public static final String MSG_LOGIN_INCOMPLETE_PROFILE = "Xác thực thành công. Vui lòng hoàn tất thông tin cá nhân của bạn.";
    public static final String MSG_REGISTER_OTP_SENT_TEMPLATE = "Mã xác thực đã được gửi thành công qua %s %s";
    public static final String MSG_REGISTER_SUCCESS_TEMPLATE = "Đăng ký thành viên thành công! Chào mừng %s.";
    public static final String MSG_TOKEN_REFRESHED = "Cấp mới Access Token thành công";
    public static final String MSG_LOGOUT_SUCCESS = "Đăng xuất thành công. Phiên làm việc đã bị hủy.";
}

