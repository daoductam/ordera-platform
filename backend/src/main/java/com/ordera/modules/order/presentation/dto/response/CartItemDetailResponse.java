package com.ordera.modules.order.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDetailResponse {

    private UUID itemId;
    private UUID productId;
    private String productName;
    private int quantity;
    private BigDecimal itemPrice;
    private BigDecimal subtotalPrice;
    private List<UUID> selectedModifierIds;
    private List<String> modifierNames;
    private String specialInstructions;
}

