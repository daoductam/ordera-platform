package com.ordera.modules.auth.domain.constant;

/**
 * Tập hợp các hằng số cấu hình nghiệp vụ và hạ tầng chuẩn hóa cho Phân hệ Auth & Identity.
 * Đặt trong tầng Domain để các tầng UseCase, Adapter và Service có thể tái sử dụng thống nhất,
 * loại bỏ hoàn toàn các hằng số trùng lặp và magic numbers.
 */
public final class AuthConstants {

    private AuthConstants() {
        // Ngăn chặn khởi tạo đối tượng tiện ích hằng số
    }

    // --- 1. Chính sách OTP (OTP Policy) ---
    /** Độ dài mặc định của mã OTP */
    public static final int OTP_LENGTH = 6;

    /** Thời gian hiệu lực của mã xác thực OTP (120 giây) */
    public static final long OTP_TTL_SECONDS = 120L;

    /** Thời gian giãn cách tối thiểu giữa 2 lần gửi mã liên tiếp (120 giây) */
    public static final long OTP_COOLDOWN_SECONDS = 120L;

    /** Số lần nhập sai OTP tối đa trước khi khóa phiên xác thực (5 lần) */
    public static final int MAX_OTP_ATTEMPTS = 5;

    /** Thời gian khóa phiên OTP khi vượt quá số lần nhập sai (900 giây = 15 phút) */
    public static final long OTP_LOCK_TTL_SECONDS = 900L;

    // --- 2. Chính sách Token JWT (Token Policy) ---
    /** Thời gian sống dự phòng của Refresh Token (7 ngày = 604,800,000 ms) */
    public static final long DEFAULT_REFRESH_TOKEN_TTL_MS = 604_800_000L;

    /** Tiền tố loại token chuẩn Bearer */
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    /** Tên claim tenantId trong payload JWT */
    public static final String CLAIM_TENANT_ID = "tenantId";

    /** Tên claim role phân quyền trong payload JWT */
    public static final String CLAIM_ROLE = "role";

    /** Tên claim loại token (access / refresh) */
    public static final String CLAIM_TYPE = "type";

    /** Giá trị loại Access Token */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /** Giá trị loại Refresh Token */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    // --- 3. Tiền tố khóa Redis (Redis Cache Key Prefixes) ---
    /** Tiền tố lưu trữ mã băm OTP */
    public static final String REDIS_OTP_PREFIX = "auth:otp:";

    /** Tiền tố kiểm soát cooldown gửi OTP */
    public static final String REDIS_COOLDOWN_PREFIX = "auth:otp:cooldown:";

    /** Tiền tố đếm số lần nhập sai OTP */
    public static final String REDIS_ATTEMPTS_PREFIX = "auth:otp:attempts:";

    /** Tiền tố lưu trữ danh sách đen Token thu hồi (Blacklist) */
    public static final String REDIS_BLACKLIST_PREFIX = "auth:blacklist:";
}

