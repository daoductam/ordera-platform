package com.ordera.modules.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model đại diện cho trạng thái đọc thông báo của từng hội viên (Fan-out on Read).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification {

    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private UUID notificationId;
    private boolean read;
    private Instant readAt;
    private Instant createdAt;

    public void markAsRead(Instant now) {
        this.read = true;
        this.readAt = now;
    }
}

