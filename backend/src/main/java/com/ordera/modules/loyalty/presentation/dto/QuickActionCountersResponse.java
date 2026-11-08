package com.ordera.modules.loyalty.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickActionCountersResponse {

    @Builder.Default
    private int activeVouchersCount = 0;

    @Builder.Default
    private int availableGiftsCount = 0;

    @Builder.Default
    private int activeOrdersCount = 0;

    @Builder.Default
    private int unreadNotificationsCount = 0;
}

