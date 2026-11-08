package com.ordera.modules.reward.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartVoucherCalculationResponse {
    private UUID cartId;
    private AppliedVoucherSummary appliedVoucher;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal vatAmount;
    private BigDecimal finalTotalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedVoucherSummary {
        private String code;
        private String title;
        private BigDecimal discountAmount;
    }
}

