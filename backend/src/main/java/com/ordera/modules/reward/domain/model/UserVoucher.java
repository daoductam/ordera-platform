package com.ordera.modules.reward.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucher {

    private UUID id;
    private UUID tenantId;
    private UUID memberId;
    private UUID voucherId;
    private String status;
    private UUID appliedOrderId;
    private Instant redeemedAt;
    private Instant usedAt;
}

