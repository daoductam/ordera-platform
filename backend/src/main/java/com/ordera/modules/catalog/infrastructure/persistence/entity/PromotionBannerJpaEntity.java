package com.ordera.modules.catalog.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "promotion_banners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionBannerJpaEntity extends BaseTenantAuditEntity {

    @Column(name = "store_id")
    private UUID storeId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "cta_label", length = 64)
    private String ctaLabel;

    @Column(name = "action_type", length = 64)
    private String actionType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}

