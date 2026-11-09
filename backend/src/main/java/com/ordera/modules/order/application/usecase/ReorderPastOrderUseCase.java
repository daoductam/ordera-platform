package com.ordera.modules.order.application.usecase;

import com.ordera.modules.order.presentation.dto.response.ReorderResponse;

import java.util.UUID;

public interface ReorderPastOrderUseCase {

    ReorderResponse execute(UUID tenantId, UUID userId, UUID orderId);
}
