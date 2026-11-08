package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Thực thể miền đại diện cho Banner tiếp thị & khuyến mãi trên đầu trang thực đơn.
 * Độc lập hoàn toàn với Spring Framework và JPA.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionBanner {

    private UUID id;
    private UUID tenantId;
    private UUID storeId;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String ctaLabel;
    private String actionType;
    private UUID targetId;
    private boolean active;
    private int displayOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
