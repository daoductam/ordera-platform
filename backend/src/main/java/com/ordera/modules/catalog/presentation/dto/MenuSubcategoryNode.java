package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record MenuSubcategoryNode(
    UUID subcategoryId,
    String subcategoryName,
    int displayOrder,
    List<ProductSummaryResponse> products
) {}
