package com.ordera.modules.loyalty.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierDefinition {

    private UUID id;
    private UUID tenantId;
    private String tierName;
    private int tierOrder;
    private int minPointsRequired;
    private String themeColorHex;
    private BigDecimal earnPointsRate;
    private List<String> benefitsSummary;
    private Instant createdAt;
}
