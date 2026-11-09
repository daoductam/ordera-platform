package com.ordera.modules.order.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

    private UUID orderId;
    private String orderNumber;
    private String orderCode;
    private String storeName;
    private String pickupMethod;
    private BigDecimal totalAmount;
    private int itemCount;
    private String firstItemName;
    private String status;
    private int estimatedReadyMinutes;
    private Instant createdAt;
}
