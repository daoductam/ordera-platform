package com.ordera.modules.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoyaltyTierDetailDto {

    private String tierName;
    private int tierOrder;
    private String themeColorHex;
    private int currentPoints;
    private int lifetimePoints;
    private int pointsToNextTier;
    private String nextTierName;
}
