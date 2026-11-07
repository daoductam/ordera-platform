package com.ordera.modules.notification.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "system_notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SystemNotificationJpaEntity extends BaseTenantEntity {

    @Column(name = "category", nullable = false, length = 32)
    @Builder.Default
    private String category = "Messages";

    @Column(name = "type", nullable = false, length = 32)
    @Builder.Default
    private String type = "system_upgrade";

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary", nullable = false)
    private String summary;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    @Column(name = "action_label", length = 64)
    private String actionLabel;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "has_support_form", nullable = false)
    @Builder.Default
    private boolean hasSupportForm = false;

    @Column(name = "support_email")
    private String supportEmail;

    @Column(name = "support_phone", length = 32)
    private String supportPhone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

