package com.ordera.modules.loyalty.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "support_claims")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SupportClaimJpaEntity extends BaseTenantEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "receipt_code", nullable = false, length = 64)
    private String receiptCode;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "bill_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal billAmount;

    @Column(name = "bill_photo_url")
    private String billPhotoUrl;

    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "points_awarded")
    private Integer pointsAwarded;

    @Column(name = "reviewer_note")
    private String reviewerNote;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}

