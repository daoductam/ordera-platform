package com.ordera.modules.auth.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Port quản lý lưu trữ OTP, đếm số lần thử và thời gian cooldown trên Redis.
 */
public interface OtpStoragePort {

    /**
     * Lưu trữ mã OTP trong bộ nhớ đệm với thời gian sống TTL.
     *
     * @param tenantId   Định danh thương hiệu
     * @param recipient  Số điện thoại hoặc email nhận mã
     * @param otpCode    Chuỗi mã OTP 6 số
     * @param ttlSeconds Thời gian sống của mã (ví dụ: 120s)
     */
    void saveOtp(UUID tenantId, String recipient, String otpCode, long ttlSeconds);

    /**
     * Lấy mã OTP hiện tại từ bộ nhớ đệm.
     */
    Optional<String> getOtp(UUID tenantId, String recipient);

    /**
     * Kiểm tra xem người nhận có đang trong thời gian Cooldown chống spam hay không.
     */
    boolean isCooldownActive(UUID tenantId, String recipient);

    /**
     * Thiết lập trạng thái Cooldown.
     */
    void setCooldown(UUID tenantId, String recipient, long cooldownSeconds);

    /**
     * Lấy thời gian Cooldown còn lại (giây).
     */
    long getCooldownRemainingSeconds(UUID tenantId, String recipient);

    /**
     * Tăng số lần thử nhập sai mã OTP.
     *
     * @return Số lần nhập sai hiện tại sau khi tăng
     */
    int incrementAttempts(UUID tenantId, String recipient, long lockTtlSeconds);

    /**
     * Lấy số lần nhập sai hiện tại.
     */
    int getAttempts(UUID tenantId, String recipient);

    /**
     * Xóa mã OTP và bộ đếm thử sau khi xác thực thành công hoặc bị khóa.
     */
    void clearOtp(UUID tenantId, String recipient);
}

