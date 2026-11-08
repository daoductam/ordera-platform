package com.ordera.modules.loyalty.presentation.constant;

/**
 * Tập hợp các thông điệp phản hồi API chuẩn hóa cho tầng Presentation của Phân hệ Loyalty & Sổ cái điểm.
 * Đặt trong tầng Presentation (presentation/constant) để tách biệt hoàn toàn với tầng Domain.
 */
public final class LoyaltyMessages {

    private LoyaltyMessages() {
        // Ngăn chặn khởi tạo đối tượng tiện ích thông điệp
    }

    public static final String MSG_PROFILE_RETRIEVED = "Member profile retrieved successfully";
    public static final String MSG_PROFILE_UPDATED = "Cập nhật thông tin cá nhân thành công";
    public static final String MSG_PROFILE_COMPLETED = "Hoàn thiện hồ sơ thành công! Tài khoản hội viên đã sẵn sàng.";
    public static final String MSG_AVATAR_UPLOADED = "Ảnh đại diện đã được cập nhật thành công.";
    public static final String MSG_TIERS_RETRIEVED = "Tier definitions retrieved successfully";
    public static final String MSG_POINTS_HISTORY_RETRIEVED = "Points ledger entries retrieved successfully";
    public static final String MSG_CLAIM_SUBMITTED = "Yêu cầu khiếu nại đã được ghi nhận. Quản trị viên sẽ xử lý trong vòng 24 giờ.";
}

