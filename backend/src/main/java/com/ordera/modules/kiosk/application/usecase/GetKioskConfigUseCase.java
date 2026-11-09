package com.ordera.modules.kiosk.application.usecase;

import com.ordera.modules.kiosk.presentation.dto.response.KioskConfigResponse;

import java.util.UUID;

public interface GetKioskConfigUseCase {

    KioskConfigResponse execute(UUID tenantId, String deviceId);
}

