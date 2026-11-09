package com.ordera.modules.kiosk.application.usecase;

import com.ordera.modules.kiosk.presentation.dto.request.KioskResetSessionRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskResetSessionResponse;

import java.util.UUID;

public interface ResetKioskSessionUseCase {

    KioskResetSessionResponse execute(UUID tenantId, String deviceId, KioskResetSessionRequest request);
}

