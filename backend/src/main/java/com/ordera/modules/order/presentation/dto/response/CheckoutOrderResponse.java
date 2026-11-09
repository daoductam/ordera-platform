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
public class CheckoutOrderResponse {

    private UUID orderId;
    private String orderNumber;
    private String orderCode;
    private String pickupMethod;
    private String tableNumber;
    private BigDecimal totalAmount;
    private int pointsEarned;
    private String status;
    private int estimatedReadyMinutes;
    private String paymentStatus;
    private String paymentMethod;
    private Instant createdAt;
}
