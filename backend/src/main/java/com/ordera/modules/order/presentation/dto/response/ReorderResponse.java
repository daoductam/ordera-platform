package com.ordera.modules.order.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderResponse {

    private UUID cartId;
    private int itemCount;
    private BigDecimal subtotalAmount;
}

