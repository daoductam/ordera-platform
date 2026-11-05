package com.ordera.modules.ledger.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "points_ledger")
@IdClass(PointsLedgerId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsLedgerJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Id
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "transaction_type", nullable = false, length = 32)
    private String transactionType;

    @Column(name = "points_delta", nullable = false)
    private int pointsDelta;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Column(name = "reference_type", length = 32)
    private String referenceType;

    @Column(name = "reference_id", length = 64)
    private String referenceId;

    @Column(name = "description", nullable = false)
    private String description;
}

