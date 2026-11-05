package com.ordera.modules.reward.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {

    private UUID id;
    private UUID tenantId;
    private String code;
    private String title;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private int pointsCost;
    private Instant validFrom;
    private Instant validUntil;
    private Integer maxIssuance;
    private boolean active;
    private Instant createdAt;
}

