package com.ordera.modules.auth.application.port;

/**
 * Outbound Port gửi tin nhắn SMS chứa mã xác thực OTP.
 */
public interface SmsSenderPort {

    /**
     * Gửi tin nhắn SMS tới số điện thoại người nhận.
     *
     * @param phoneNumber Số điện thoại theo chuẩn quốc tế E.164
     * @param message     Nội dung tin nhắn
     */
    void sendSms(String phoneNumber, String message);
}

