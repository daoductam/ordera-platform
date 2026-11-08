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
public class RedeemVoucherResponse {
    private UUID userVoucherId;
    private String code;
    private String title;
    private int pointsSpent;
    private int remainingPoints;
    private Instant validUntil;
    private String status;
}
