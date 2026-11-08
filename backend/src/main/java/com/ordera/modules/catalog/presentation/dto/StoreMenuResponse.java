package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record StoreMenuResponse(
    UUID storeId,
    List<MenuCategoryNode> categories
) {}
