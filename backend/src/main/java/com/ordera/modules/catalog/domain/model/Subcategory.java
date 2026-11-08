package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Thực thể miền đại diện cho Danh mục con (Subcategory Pills: Fresh maki, Fried maki...).
 * Độc lập hoàn toàn với Spring Framework và JPA.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subcategory {

    private UUID id;
    private UUID categoryId;
    private String name;
    private int displayOrder;
    private boolean active;
    private Instant createdAt;
}
