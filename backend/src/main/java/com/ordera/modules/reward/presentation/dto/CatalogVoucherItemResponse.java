package com.ordera.modules.reward.presentation.dto;

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
public class CatalogVoucherItemResponse {
    private UUID id;
    private String code;
    private String title;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private int pointsCost;
    private BigDecimal minOrderValue;
    private Instant validUntil;
    private boolean canAfford;
    private int pointsNeeded;
}

