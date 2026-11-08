package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Thực thể miền đại diện cho Món ăn / Đồ uống trong thực đơn.
 * Độc lập hoàn toàn với Spring Framework và JPA.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private UUID id;
    private UUID tenantId;
    private UUID categoryId;
    private UUID subcategoryId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal originalPrice;
    private String piecesText;
    @Builder.Default
    private List<String> dietaryTags = new ArrayList<>();
    private String imageUrl;
    @Builder.Default
    private boolean available = true;
    @Builder.Default
    private BigDecimal vatRate = new BigDecimal("15.00");
    @Builder.Default
    private List<ModifierGroup> modifierGroups = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
}
