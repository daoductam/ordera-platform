package com.ordera.modules.order.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    private UUID orderId;
    private String orderNumber;
    private String orderCode;
    private StoreInfoResponse store;
    private String status;
    @Builder.Default
    private List<TimelineStepResponse> timeline = new ArrayList<>();
    private int estimatedReadyMinutes;
    private String pickupMethod;
    private String tableNumber;
    @Builder.Default
    private List<OrderDetailItemResponse> items = new ArrayList<>();
    private FinancialSummaryResponse financialSummary;
    private int pointsEarned;
}

