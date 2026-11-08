package com.ordera.modules.reward.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardsCatalogResponse {
    private int memberAvailablePoints;
    private List<CatalogVoucherItemResponse> vouchers;
    private List<CatalogGiftItemResponse> gifts;
}
