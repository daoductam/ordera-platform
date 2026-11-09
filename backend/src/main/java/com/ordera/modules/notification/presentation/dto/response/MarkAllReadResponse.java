package com.ordera.modules.notification.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phản hồi đánh dấu tất cả thông báo đã đọc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkAllReadResponse {

    private long markedCount;
    private long unreadCount;
}

