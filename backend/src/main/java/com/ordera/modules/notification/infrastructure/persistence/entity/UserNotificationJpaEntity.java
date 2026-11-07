package com.ordera.modules.notification.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_notifications", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_notifications", columnNames = {"tenant_id", "user_id", "notification_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserNotificationJpaEntity extends BaseTenantEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

