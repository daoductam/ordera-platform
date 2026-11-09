package com.ordera.modules.order.application.usecase;

import com.ordera.modules.order.presentation.dto.response.SendEReceiptResponse;

import java.util.UUID;

public interface SendEReceiptEmailUseCase {

    SendEReceiptResponse execute(UUID tenantId, UUID userId, UUID orderId, String email);
}
