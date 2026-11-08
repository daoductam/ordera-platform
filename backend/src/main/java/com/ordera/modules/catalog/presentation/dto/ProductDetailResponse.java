package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record ProductDetailResponse(
    UUID productId,
    String productName,
    String description,
    BigDecimal basePrice,
    BigDecimal originalPrice,
    String piecesText,
    List<String> dietaryTags,
    String imageUrl,
    BigDecimal vatRate,
    boolean isAvailable,
    List<ModifierGroupResponse> modifierGroups
) {}
