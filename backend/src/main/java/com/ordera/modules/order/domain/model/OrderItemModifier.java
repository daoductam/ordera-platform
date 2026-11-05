package com.ordera.modules.order.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemModifier {

    private UUID id;
    private UUID orderItemId;
    private String modifierGroupName;
    private String modifierName;
    private BigDecimal extraPrice;
}
