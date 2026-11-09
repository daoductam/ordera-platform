package com.ordera.modules.kiosk.application.usecase;

import com.ordera.modules.kiosk.presentation.dto.request.KioskIdentifyRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskCustomerInfoResponse;

import java.util.UUID;

public interface KioskIdentifyCustomerUseCase {

    KioskCustomerInfoResponse execute(UUID tenantId, String deviceId, KioskIdentifyRequest request);
}

