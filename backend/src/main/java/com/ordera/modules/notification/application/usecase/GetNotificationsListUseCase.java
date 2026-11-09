package com.ordera.modules.notification.application.usecase;

import com.ordera.common.api.PageResponse;
import com.ordera.modules.notification.presentation.dto.response.NotificationSummaryResponse;

import java.util.UUID;

/**
 * UseCase 8.2: Lấy danh sách thông báo phân trang theo tab Messages hoặc News.
 */
public interface GetNotificationsListUseCase {

    PageResponse<NotificationSummaryResponse> execute(
            UUID tenantId,
            UUID userId,
            String type,
            Boolean isRead,
            int page,
            int size
    );
}

