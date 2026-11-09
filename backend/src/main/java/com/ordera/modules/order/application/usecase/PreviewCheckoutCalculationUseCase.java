package com.ordera.modules.order.application.usecase;

import com.ordera.modules.order.presentation.dto.request.PreviewCheckoutRequest;
import com.ordera.modules.order.presentation.dto.response.PreviewCheckoutResponse;

import java.util.UUID;

public interface PreviewCheckoutCalculationUseCase {

    PreviewCheckoutResponse execute(UUID tenantId, UUID userId, PreviewCheckoutRequest request);
}

