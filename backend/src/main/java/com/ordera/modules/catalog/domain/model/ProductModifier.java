package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductModifier {

    private UUID id;
    private UUID groupId;
    private String name;
    private BigDecimal extraPrice;
    private boolean isDefault;
}
