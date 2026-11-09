package com.ordera.modules.order.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewCheckoutResponse {

    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal tipAmount;
    private BigDecimal vatAmount;
    private double vatPercentage;
    private BigDecimal finalTotalAmount;
    private String currency;
    private int pointsToEarn;
    private AppliedVoucherResponse appliedVoucher;
    private SelectedGiftResponse selectedGift;
}

