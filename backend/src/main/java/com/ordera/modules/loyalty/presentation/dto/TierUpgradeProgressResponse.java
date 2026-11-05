package com.ordera.modules.loyalty.presentation.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierUpgradeProgressResponse {

    private String nextTierName;
    private int nextTierRequiredPoints;
    private int pointsNeeded;
    private double progressPercentage;
}

