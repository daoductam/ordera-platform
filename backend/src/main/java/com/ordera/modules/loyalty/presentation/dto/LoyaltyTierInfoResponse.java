package com.ordera.modules.loyalty.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTierInfoResponse {

    private String currentTier;
    private int tierOrder;
    private String themeColorHex;
    private BigDecimal earnPointsRate;
    private int currentPoints;
    private int lifetimePoints;
    private int pointsToNextTier;
    private String nextTierName;
    private int nextTierMinPoints;
    private double progressPercentage;
}

