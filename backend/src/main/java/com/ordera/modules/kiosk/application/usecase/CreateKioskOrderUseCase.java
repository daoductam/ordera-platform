package com.ordera.modules.kiosk.application.usecase;

import com.ordera.modules.kiosk.presentation.dto.request.KioskCheckoutRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskCheckoutResponse;

import java.util.UUID;

public interface CreateKioskOrderUseCase {

    KioskCheckoutResponse execute(UUID tenantId, String deviceId, KioskCheckoutRequest request);
}

