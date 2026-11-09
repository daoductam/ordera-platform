package com.ordera.modules.notification.application.usecase;

import com.ordera.modules.notification.presentation.dto.response.UnreadCountResponse;

import java.util.UUID;

/**
 * UseCase 8.1: Lấy số lượng thông báo chưa đọc của hội viên hiển thị badge đỏ trên icon quả chuông.
 */
public interface GetUnreadNotificationCountUseCase {

    UnreadCountResponse execute(UUID tenantId, UUID userId);
}

