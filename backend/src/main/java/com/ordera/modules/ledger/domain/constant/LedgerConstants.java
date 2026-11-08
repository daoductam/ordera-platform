package com.ordera.modules.ledger.domain.constant;

/**
 * Tập hợp các hằng số cấu hình nghiệp vụ và phân trang cho Phân hệ Sổ cái điểm bất biến (Points Ledger).
 * Đặt trong tầng Domain (domain/constant) theo chuẩn Clean Architecture.
 */
public final class LedgerConstants {

    private LedgerConstants() {
        // Ngăn chặn khởi tạo đối tượng tiện ích hằng số
    }

    /** Trang mặc định khi truy vấn lịch sử sổ cái (trang đầu tiên) */
    public static final int DEFAULT_PAGE_NUMBER = 0;

    /** Số bản ghi mặc định trên mỗi trang lịch sử sổ cái */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** Số bản ghi tối đa cho phép trên mỗi trang nhằm chống tràn bộ nhớ và tấn công DoS */
    public static final int MAX_PAGE_SIZE = 50;

    /** Loại tham chiếu giao dịch: Đơn hàng */
    public static final String REF_TYPE_ORDERS = "ORDERS";

    /** Loại tham chiếu giao dịch: Phiếu khiếu nại thiếu điểm */
    public static final String REF_TYPE_SUPPORT_CLAIMS = "SUPPORT_CLAIMS";

    /** Loại tham chiếu giao dịch: Đổi voucher giảm giá */
    public static final String REF_TYPE_VOUCHERS = "VOUCHERS";
}

