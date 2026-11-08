package com.ordera.modules.loyalty.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierDefinitionResponse {

    private UUID id;
    private String tierName;
    private int tierOrder;
    private int minPointsRequired;
    private String themeColorHex;
    private BigDecimal earnPointsRate;
    private List<String> benefitsSummary;
}

