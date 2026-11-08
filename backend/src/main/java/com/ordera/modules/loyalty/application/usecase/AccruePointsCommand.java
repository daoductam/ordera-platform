package com.ordera.modules.loyalty.application.usecase;

import com.ordera.modules.ledger.domain.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccruePointsCommand {

    private UUID tenantId;
    private UUID memberId;
    private TransactionType transactionType;
    private int pointsDelta;
    private String referenceType;
    private String referenceId;
    private String description;
}

