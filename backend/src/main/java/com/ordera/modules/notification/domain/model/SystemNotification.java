package com.ordera.modules.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model đại diện cho thông báo hệ thống hoặc chiến dịch do Admin phát hành.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotification {

    private UUID id;
    private UUID tenantId;
    private String category;
    private String type;
    private String title;
    private String summary;
    private String content;
    private String bannerImageUrl;
    private String actionLabel;
    private String actionUrl;
    private boolean hasSupportForm;
    private String supportEmail;
    private String supportPhone;
    private boolean active;
    private Instant expiresAt;
    private Instant createdAt;

    public boolean isExpired(Instant now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }
}

