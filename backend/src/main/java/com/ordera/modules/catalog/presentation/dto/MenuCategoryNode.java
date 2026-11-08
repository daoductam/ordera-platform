package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record MenuCategoryNode(
    UUID categoryId,
    String categoryName,
    String icon,
    int displayOrder,
    List<MenuSubcategoryNode> subcategories
) {}
