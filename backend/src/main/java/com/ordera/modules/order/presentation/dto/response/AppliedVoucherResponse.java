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
public class AppliedVoucherResponse {

    private String code;
    private String title;
    private String discountValueText;
    private BigDecimal discountAmount;
}

