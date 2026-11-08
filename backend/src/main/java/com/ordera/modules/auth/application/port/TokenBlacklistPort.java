package com.ordera.modules.auth.application.port;

/**
 * Outbound Port quản lý danh sách đen Token (Token Blacklist) trên Redis sau khi đăng xuất.
 */
public interface TokenBlacklistPort {

    /**
     * Đưa định danh JWT (jti) vào danh sách đen với thời gian sống còn lại.
     *
     * @param jti            ID định danh token (JWT ID)
     * @param remainingTtlMs Thời gian sống còn lại của token (mili-giây)
     */
    void blacklistToken(String jti, long remainingTtlMs);

    /**
     * Kiểm tra xem token có nằm trong danh sách đen hay không.
     *
     * @param jti ID định danh token (JWT ID)
     * @return true nếu token đã bị thu hồi / đăng xuất
     */
    boolean isBlacklisted(String jti);
}

