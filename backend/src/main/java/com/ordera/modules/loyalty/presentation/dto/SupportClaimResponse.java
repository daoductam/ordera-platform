package com.ordera.modules.loyalty.presentation.dto;

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
public class SupportClaimResponse {

    private UUID claimId;
    private String receiptCode;
    private BigDecimal billAmount;
    private UUID storeId;
    private String status;
    private Instant createdAt;
}

