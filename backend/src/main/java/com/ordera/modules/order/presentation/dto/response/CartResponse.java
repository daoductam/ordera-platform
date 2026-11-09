package com.ordera.modules.order.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private UUID cartId;
    private UUID storeId;
    private String storeName;
    private int totalItemCount;
    @Builder.Default
    private List<CartItemDetailResponse> items = new ArrayList<>();
    private String comment;
    private String appliedVoucherCode;
    private UUID appliedGiftId;
    @Builder.Default
    private BigDecimal subtotalAmount = BigDecimal.ZERO;
    private String currency;
}
