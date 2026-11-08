package com.ordera.modules.loyalty.domain.constant;

import java.util.Set;

/**
 * Tập hợp các hằng số cấu hình nghiệp vụ và hạ tầng chuẩn hóa cho Phân hệ Loyalty & Thẻ thành viên.
 * Đặt trong tầng Domain (domain/constant) theo chuẩn Clean Architecture tương tự AuthConstants,
 * giúp các UseCase, Adapter, Entity và Controller tái sử dụng thống nhất, loại bỏ hoàn toàn magic numbers.
 */
public final class LoyaltyConstants {

    private LoyaltyConstants() {
        // Ngăn chặn khởi tạo đối tượng tiện ích hằng số
    }

    // --- 1. Tải lên ảnh đại diện (Avatar Upload Policy) ---
    /** Dung lượng file ảnh đại diện tối đa cho phép (5 MB = 5 * 1024 * 1024 bytes) */
    public static final long MAX_AVATAR_FILE_SIZE = 5L * 1024L * 1024L;

    /** Domain lưu trữ ảnh đại diện mặc định */
    public static final String DEFAULT_AVATAR_STORAGE_BASE_URL = "https://storage.ordera.no/avatars";

    /** Danh sách định dạng MIME types hợp lệ khi tải ảnh đại diện */
    public static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    // --- 2. Khóa phân tán Redisson (Distributed Lock Policy) ---
    /** Thời gian chờ tối đa để chiếm khóa giao dịch điểm (3 giây) */
    public static final long REDIS_LOCK_WAIT_SECONDS = 3L;

    /** Thời gian tự động giải phóng khóa phân tán nếu tiến trình gặp sự cố (5 giây) */
    public static final long REDIS_LOCK_LEASE_SECONDS = 5L;

    /** Khuôn mẫu định dạng khóa phân tán Redisson chống Race Condition tích điểm */
    public static final String REDIS_LOCK_KEY_PATTERN = "lock:tenant:%s:member:%s";

    // --- 3. Khiếu nại thiếu điểm hóa đơn (Support Claim Policy) ---
    /** Số ngày tối đa cho phép khiếu nại tính từ ngày xuất hóa đơn (30 ngày) */
    public static final int MAX_ALLOWED_RECEIPT_DAYS = 30;

    /** Trạng thái phiếu khiếu nại: Chờ duyệt */
    public static final String CLAIM_STATUS_PENDING = "PENDING";

    /** Trạng thái phiếu khiếu nại: Đã phê duyệt và cộng điểm */
    public static final String CLAIM_STATUS_APPROVED = "APPROVED";

    /** Trạng thái phiếu khiếu nại: Bị từ chối */
    public static final String CLAIM_STATUS_REJECTED = "REJECTED";

    // --- 4. Hồ sơ hội viên & Barcode (Member Profile & Barcode Defaults) ---
    /** Tiền tố mặc định của mã barcode định danh hội viên */
    public static final String DEFAULT_BARCODE_PREFIX = "ORD-MEM-";

    /** Base URL của hệ sinh thái Ordera */
    public static final String DEFAULT_API_BASE_URL = "https://api.ordera.no";

    /** Định dạng URL ảnh Barcode QR */
    public static final String BARCODE_URL_TEMPLATE = "%s/v1/barcodes/%s.png";

    /** Giá trị đếm mặc định cho các widget hành động nhanh */
    public static final int DEFAULT_ACTIVE_VOUCHERS_COUNT = 0;
    public static final int DEFAULT_AVAILABLE_GIFTS_COUNT = 0;
    public static final int DEFAULT_ACTIVE_ORDERS_COUNT = 0;
}

