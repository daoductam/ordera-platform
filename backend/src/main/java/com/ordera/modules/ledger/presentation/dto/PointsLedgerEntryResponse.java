package com.ordera.modules.ledger.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsLedgerEntryResponse {

    private UUID id;
    private String transactionType;
    private int pointsDelta;
    private int balanceAfter;
    private String referenceType;
    private String referenceId;
    private String description;
    private Instant createdAt;
}

