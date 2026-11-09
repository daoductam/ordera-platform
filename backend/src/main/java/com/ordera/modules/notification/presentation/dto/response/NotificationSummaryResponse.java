package com.ordera.modules.notification.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO tóm tắt thông báo hiển thị trên danh sách (Figma 05.02).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSummaryResponse {

    private UUID id;
    private String type;
    private String category;
    private String title;
    private String summary;
    private String date;
    private Instant createdAt;
    private boolean isRead;
    private String bannerImageUrl;
    private String actionLabel;
    private String actionUrl;
}

