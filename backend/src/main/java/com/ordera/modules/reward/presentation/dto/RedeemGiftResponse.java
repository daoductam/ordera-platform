package com.ordera.modules.reward.presentation.dto;

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
public class RedeemGiftResponse {
    private UUID redemptionId;
    private String giftTitle;
    private int pointsSpent;
    private int remainingPoints;
    private String redemptionCode;
    private String status;
    private String instruction;
    private Instant createdAt;
}
