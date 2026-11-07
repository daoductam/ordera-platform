package com.ordera.modules.order.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders", uniqueConstraints = {
        @UniqueConstraint(name = "uq_orders_tenant_code", columnNames = {"tenant_id", "order_code"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderJpaEntity extends BaseTenantEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "order_number", length = 16)
    private String orderNumber;

    @Column(name = "order_code", nullable = false, length = 32)
    private String orderCode;

    @Column(name = "pickup_method", nullable = false, length = 32)
    @Builder.Default
    private String pickupMethod = "COUNTER";

    @Column(name = "pickup_time_type", nullable = false, length = 32)
    @Builder.Default
    private String pickupTimeType = "ASAP";

    @Column(name = "scheduled_pickup_at")
    private Instant scheduledPickupAt;

    @Column(name = "table_number", length = 32)
    private String tableNumber;

    @Column(name = "pickup_person_name", nullable = false, length = 128)
    private String pickupPersonName;

    @Column(name = "pickup_person_phone", nullable = false, length = 32)
    private String pickupPersonPhone;

    @Column(name = "subtotal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tip_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tipAmount = BigDecimal.ZERO;

    @Column(name = "vat_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal vatAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "points_earned", nullable = false)
    @Builder.Default
    private int pointsEarned = 0;

    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "payment_status", nullable = false, length = 32)
    @Builder.Default
    private String paymentStatus = "UNPAID";

    @Column(name = "payment_method", length = 32)
    private String paymentMethod;

    @Column(name = "order_notes")
    private String orderNotes;

    @Column(name = "applied_voucher_id")
    private UUID appliedVoucherId;

    @Column(name = "applied_gift_id")
    private UUID appliedGiftId;

    @Column(name = "applied_gift_title", length = 128)
    private String appliedGiftTitle;

    @Column(name = "e_receipt_email")
    private String eReceiptEmail;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;
}

