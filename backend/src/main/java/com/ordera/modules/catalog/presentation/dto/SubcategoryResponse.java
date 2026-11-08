package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.util.UUID;

@Builder
public record SubcategoryResponse(
    UUID id,
    String name,
    int displayOrder
) {}
