package com.ordera.modules.order.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private UUID id;
    private UUID cartId;
    private UUID productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotalPrice;
    @Builder.Default
    private List<UUID> selectedModifierIds = new ArrayList<>();
    @Builder.Default
    private List<String> modifierNames = new ArrayList<>();
    private String specialInstruction;
    private Instant createdAt;
}

