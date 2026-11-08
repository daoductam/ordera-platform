package com.ordera.modules.reward.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartOfferVoucherItemResponse {
    private String code;
    private String title;
    private String discountType;
    private BigDecimal discountValue;
    private String discountValueText;
    private BigDecimal minOrderValue;
    private boolean isApplicable;
    private String ineligibilityReason;
    private String status;
}

