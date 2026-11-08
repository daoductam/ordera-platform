package com.ordera.modules.auth.application.port;

/**
 * Outbound Port gửi Email chứa mã xác thực OTP hoặc thông báo.
 */
public interface EmailSenderPort {

    /**
     * Gửi Email tới địa chỉ người nhận.
     *
     * @param toRecipient Địa chỉ email người nhận
     * @param subject     Tiêu đề email
     * @param content     Nội dung email
     */
    void sendEmail(String toRecipient, String subject, String content);
}
