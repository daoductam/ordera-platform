package com.ordera.modules.notification.application.usecase;

import com.ordera.modules.notification.presentation.dto.response.MarkReadResponse;

import java.util.UUID;

/**
 * UseCase 8.4: Đánh dấu một thông báo cụ thể từ chưa đọc sang đã đọc.
 */
public interface MarkNotificationReadUseCase {

    MarkReadResponse execute(UUID tenantId, UUID userId, UUID notificationId);
}

