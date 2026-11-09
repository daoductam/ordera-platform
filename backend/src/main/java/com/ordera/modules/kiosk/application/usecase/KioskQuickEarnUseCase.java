package com.ordera.modules.kiosk.application.usecase;

import com.ordera.modules.kiosk.presentation.dto.request.KioskQuickEarnRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskQuickEarnResponse;

import java.util.UUID;

public interface KioskQuickEarnUseCase {

    KioskQuickEarnResponse execute(UUID tenantId, String deviceId, String staffPin, KioskQuickEarnRequest request);
}

