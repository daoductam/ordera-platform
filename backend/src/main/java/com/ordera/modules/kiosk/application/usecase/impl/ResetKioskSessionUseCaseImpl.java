package com.ordera.modules.kiosk.application.usecase.impl;

import com.ordera.modules.kiosk.application.usecase.ResetKioskSessionUseCase;
import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.infrastructure.adapter.RedisKioskSessionAdapter;
import com.ordera.modules.kiosk.presentation.dto.request.KioskResetSessionRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskResetSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * Orchestrator UseCase: Reset phiên làm việc Kiosk về Standby (Idle 60s hoặc Start Over).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetKioskSessionUseCaseImpl implements ResetKioskSessionUseCase {

    private final RedisKioskSessionAdapter redisKioskSessionAdapter;

    @Override
    public KioskResetSessionResponse execute(UUID tenantId, String deviceId, KioskResetSessionRequest request) {
        validateInputs(tenantId, deviceId, request);
        clearTemporarySession(tenantId, deviceId, request.getReason());

        return KioskResetSessionResponse.builder()
                .kioskStatus(KioskConstants.KIOSK_STATUS_STANDBY)
                .countdownDefaultSeconds(KioskConstants.WATCHDOG_IDLE_TIMEOUT_SECONDS)
                .build();
    }

    private void validateInputs(UUID tenantId, String deviceId, KioskResetSessionRequest request) {
        Objects.requireNonNull(tenantId, "TenantId must not be null");
        Objects.requireNonNull(deviceId, "DeviceId must not be null");
        Objects.requireNonNull(request, "Request must not be null");
    }

    private void clearTemporarySession(UUID tenantId, String deviceId, String reason) {
        log.info("Resetting kiosk session for device: {} (tenant: {}, reason: {})", deviceId, tenantId, reason);
        redisKioskSessionAdapter.clearSession(tenantId, deviceId);
    }
}

