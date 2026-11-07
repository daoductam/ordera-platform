package com.ordera.modules.reward.infrastructure.persistence.entity;

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
@Table(name = "loyalty_gifts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class LoyaltyGiftJpaEntity extends BaseTenantEntity {

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "points_cost", nullable = false)
    @Builder.Default
    private int pointsCost = 100;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private int stockQuantity = 10;

    @Column(name = "tag", length = 64)
    private String tag;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
