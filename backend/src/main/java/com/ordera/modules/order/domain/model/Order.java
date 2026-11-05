package com.ordera.modules.order.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private UUID id;
    private UUID tenantId;
    private UUID storeId;
    private UUID userId;
    private String orderCode;
    private String pickupMethod;
    private String pickupTimeType;
    private Instant scheduledPickupAt;
    private String tableNumber;
    private String pickupPersonName;
    private String pickupPersonPhone;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal tipAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private int pointsEarned;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private String orderNotes;
    private UUID appliedVoucherId;
    private Instant createdAt;
    private Instant completedAt;
}
