package com.ordera.modules.catalog.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "product_modifier_groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductModifierGroupJpaEntity extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "min_selection", nullable = false)
    @Builder.Default
    private int minSelection = 0;

    @Column(name = "max_selection", nullable = false)
    @Builder.Default
    private int maxSelection = 1;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}

