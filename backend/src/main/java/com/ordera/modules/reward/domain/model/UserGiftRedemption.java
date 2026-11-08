package com.ordera.modules.reward.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGiftRedemption {

    private UUID id;
    private UUID tenantId;
    private UUID memberId;
    private UUID giftId;
    private int pointsSpent;
    private String redemptionCode;
    private String status;
    private UUID appliedOrderId;
    private Instant claimedAt;
    private Instant createdAt;
}
