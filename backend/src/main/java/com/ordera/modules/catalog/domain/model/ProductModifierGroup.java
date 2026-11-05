package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductModifierGroup {

    private UUID id;
    private UUID productId;
    private String name;
    private int minSelection;
    private int maxSelection;
    private int displayOrder;
}
