package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Thực thể miền đại diện cho Danh mục món ăn / đồ uống chính.
 * Độc lập hoàn toàn với Spring Framework và JPA.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String icon;
    private int displayOrder;
    private boolean active;
    @Builder.Default
    private List<Subcategory> subcategories = new ArrayList<>();
    private Instant createdAt;
}
