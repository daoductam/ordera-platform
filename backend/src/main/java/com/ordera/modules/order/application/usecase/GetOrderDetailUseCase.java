package com.ordera.modules.order.application.usecase;

import com.ordera.modules.order.presentation.dto.response.OrderDetailResponse;

import java.util.UUID;

public interface GetOrderDetailUseCase {

    OrderDetailResponse execute(UUID tenantId, UUID userId, UUID orderId);
}

