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
public class Cart {

    private UUID id;
    private UUID tenantId;
    private UUID storeId;
    private UUID userId;
    private String comment;
    private String appliedVoucherCode;
    private UUID appliedGiftId;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    @Builder.Default
    private BigDecimal subtotalAmount = BigDecimal.ZERO;
    private Instant createdAt;
    private Instant updatedAt;
}

