package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
public record ProductSummaryResponse(
    UUID id,
    String name,
    String description,
    BigDecimal basePrice,
    BigDecimal originalPrice,
    String piecesText,
    List<String> dietaryTags,
    String categoryName,
    String imageUrl,
    boolean isPopular,
    boolean isAvailable,
    boolean hasModifiers
) {}
