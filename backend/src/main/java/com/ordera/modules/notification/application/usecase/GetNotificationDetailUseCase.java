package com.ordera.modules.notification.application.usecase;

import com.ordera.modules.notification.presentation.dto.response.NotificationDetailResponse;

import java.util.UUID;

/**
 * UseCase 8.3: Lấy chi tiết bài viết, banner ảnh và tự động đánh dấu đã đọc.
 */
public interface GetNotificationDetailUseCase {

    NotificationDetailResponse execute(UUID tenantId, UUID userId, UUID notificationId);
}

