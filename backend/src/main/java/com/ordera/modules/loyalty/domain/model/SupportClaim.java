package com.ordera.modules.loyalty.domain.model;

import com.ordera.modules.loyalty.domain.constant.LoyaltyConstants;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportClaim {

    public static final String STATUS_PENDING = LoyaltyConstants.CLAIM_STATUS_PENDING;
    public static final String STATUS_APPROVED = LoyaltyConstants.CLAIM_STATUS_APPROVED;
    public static final String STATUS_REJECTED = LoyaltyConstants.CLAIM_STATUS_REJECTED;

    private UUID id;
    private UUID tenantId;
    private UUID storeId;
    private UUID memberId;
    private String receiptCode;
    private LocalDate orderDate;
    private BigDecimal billAmount;
    private String billPhotoUrl;
    private String status;
    private Integer pointsAwarded;
    private String reviewerNote;
    private Instant createdAt;
    private Instant resolvedAt;
}
