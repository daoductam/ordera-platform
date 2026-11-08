package com.ordera.modules.reward.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableOffersResponse {
    private BigDecimal subtotalAmount;
    private List<CartOfferVoucherItemResponse> vouchers;
    private List<CartOfferGiftItemResponse> gifts;
}
