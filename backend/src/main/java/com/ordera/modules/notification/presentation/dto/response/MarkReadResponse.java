package com.ordera.modules.notification.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO phản hồi đánh dấu 1 thông báo đã đọc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkReadResponse {

    private UUID id;
    private boolean isRead;
    private Instant readAt;
}

