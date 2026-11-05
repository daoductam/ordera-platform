package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private UUID id;
    private UUID tenantId;
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String imageUrl;
    private boolean available;
    private BigDecimal vatRate;
    private Instant createdAt;
    private Instant updatedAt;
}
