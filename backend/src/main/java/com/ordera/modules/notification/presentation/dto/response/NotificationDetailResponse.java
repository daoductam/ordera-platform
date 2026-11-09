package com.ordera.modules.notification.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO chi tiết thông báo hiển thị trên Modal 05.03.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDetailResponse {

    private UUID id;
    private String category;
    private String type;
    private String title;
    private String bannerImageUrl;
    private String content;
    private boolean isRead;
    private Instant createdAt;
    private boolean hasSupportForm;
    private String supportEmail;
    private String supportPhone;
}

