package com.ordera.modules.notification.domain.model;

/**
 * Trạng thái xử lý phiếu khiếu nại thất lạc điểm:
 * PENDING: Chờ tiếp nhận
 * IN_REVIEW: Đang đối soát với cơ sở dữ liệu cũ
 * RESOLVED: Đã giải quyết & hoàn điểm bù
 * REJECTED: Từ chối giải quyết (không tìm thấy hóa đơn/gian lận)
 */
public enum InquiryStatus {
    PENDING,
    IN_REVIEW,
    RESOLVED,
    REJECTED
}

