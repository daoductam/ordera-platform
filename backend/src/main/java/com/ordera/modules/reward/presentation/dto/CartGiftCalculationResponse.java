package com.ordera.modules.reward.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartGiftCalculationResponse {
    private UUID cartId;
    private SelectedGiftSummary selectedGift;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedGiftSummary {
        private UUID giftId;
        private String title;
        private String status;
    }
}

