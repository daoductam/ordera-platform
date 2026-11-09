package com.ordera.modules.notification.application.usecase;

import com.ordera.modules.notification.presentation.dto.response.MarkAllReadResponse;

import java.util.UUID;

/**
 * UseCase 8.5: Đánh dấu tất cả thông báo trong hộp thư của hội viên thành đã đọc.
 */
public interface MarkAllNotificationsReadUseCase {

    MarkAllReadResponse execute(UUID tenantId, UUID userId);
}

