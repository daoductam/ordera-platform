package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.util.UUID;

@Builder
public record PromotionBannerResponse(
    UUID id,
    String title,
    String subtitle,
    String imageUrl,
    String ctaLabel,
    String ctaActionType,
    UUID targetCategoryId,
    boolean active,
    int displayOrder
) {}
