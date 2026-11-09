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
public class FinancialSummaryResponse {

    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal tipAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
}

