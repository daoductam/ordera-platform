package com.ordera.modules.ledger.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsLedger {

    private UUID id;
    private Instant createdAt;
    private UUID tenantId;
    private UUID memberId;
    private String transactionType;
    private int pointsDelta;
    private int balanceAfter;
    private String referenceType;
    private String referenceId;
    private String description;
}
