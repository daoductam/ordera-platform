package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record CategoryResponse(
    UUID id,
    String name,
    int displayOrder,
    String icon,
    List<SubcategoryResponse> subcategories
) {}
