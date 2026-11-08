package com.ordera.modules.reward.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucherItemResponse {
    private UUID userVoucherId;
    private UUID voucherId;
    private String code;
    private String title;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private String discountValueText;
    private BigDecimal minOrderValue;
    private Instant validFrom;
    private Instant validUntil;
    private String status;
}

