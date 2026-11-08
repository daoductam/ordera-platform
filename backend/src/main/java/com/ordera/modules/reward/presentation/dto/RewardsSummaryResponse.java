package com.ordera.modules.reward.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardsSummaryResponse {
    private int totalAvailableRewards;
    private int activeVouchersCount;
    private int availableGiftsCount;
}
