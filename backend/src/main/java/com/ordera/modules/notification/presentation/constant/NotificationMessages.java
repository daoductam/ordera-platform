package com.ordera.modules.notification.presentation.constant;

import lombok.experimental.UtilityClass;

/**
 * Tập trung chuỗi phản hồi thành công và thông báo lỗi cho Phân hệ Thông Báo & Hỗ Trợ Khách Hàng.
 * Tuyệt đối cấm hardcode string trong ApiResponse và Controllers.
 */
@UtilityClass
public class NotificationMessages {

    public static final String MSG_UNREAD_COUNT_RETRIEVED = "Unread count retrieved";
    public static final String MSG_NOTIFICATIONS_LIST_RETRIEVED = "Notifications retrieved successfully";
    public static final String MSG_NOTIFICATION_DETAIL_RETRIEVED = "Notification detail retrieved successfully";
    public static final String MSG_NOTIFICATION_MARKED_READ = "Notification marked as read";
    public static final String MSG_ALL_NOTIFICATIONS_MARKED_READ = "All notifications marked as read";
    public static final String MSG_INQUIRY_SUBMITTED = "Phiếu yêu cầu tra cứu điểm đã được gửi thành công. Đội ngũ hỗ trợ sẽ đối soát và phản hồi trong 24 giờ.";

    public static final String ERR_TENANT_REQUIRED = "Header 'X-Tenant-ID' is required for multi-tenant data isolation.";
    public static final String ERR_UNAUTHORIZED = "Vui lòng đăng nhập để thực hiện thao tác này.";
    public static final String ERR_NOTIFICATION_NOT_FOUND = "Không tìm thấy thông báo với ID được chỉ định.";
    public static final String ERR_INQUIRY_VALIDATION = "Thông tin phiếu khiếu nại không hợp lệ hoặc vượt quá giới hạn.";
    public static final String ERR_EMAIL_REQUIRED = "Email nhận kết quả là bắt buộc và phải đúng định dạng.";
    public static final String ERR_PHONE_REQUIRED = "Số điện thoại là bắt buộc và phải có ít nhất 8 chữ số.";
    public static final String ERR_NOTE_LENGTH = "Nội dung cần hỗ trợ phải từ 5 đến 500 ký tự.";
}

