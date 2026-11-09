package com.ordera.modules.order.application.usecase;

import com.ordera.modules.order.presentation.dto.request.CheckoutOrderRequest;
import com.ordera.modules.order.presentation.dto.response.CheckoutOrderResponse;

import java.util.UUID;

public interface CheckoutOrderUseCase {

    CheckoutOrderResponse execute(UUID tenantId, UUID userId, String idempotencyKey, CheckoutOrderRequest request);
}

