package com.ordera.modules.kiosk.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskQuickEarnResponse {

    private String transactionId;
    private String phoneNumber;
    private String memberName;
    private BigDecimal billAmount;
    private String currency;
    private int pointsEarned;
    private int newTotalPoints;
    private String currentTier;
    private String queueNumber;
}

