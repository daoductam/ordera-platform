package com.ordera.modules.kiosk.application.usecase;

import com.ordera.modules.kiosk.application.usecase.impl.ResetKioskSessionUseCaseImpl;
import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.infrastructure.adapter.RedisKioskSessionAdapter;
import com.ordera.modules.kiosk.presentation.dto.request.KioskResetSessionRequest;
import com.ordera.modules.kiosk.presentation.dto.response.KioskResetSessionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResetKioskSessionUseCase Unit Tests")
class ResetKioskSessionUseCaseTest {

    @Mock
    private RedisKioskSessionAdapter redisKioskSessionAdapter;

    @InjectMocks
    private ResetKioskSessionUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final String deviceId = "kiosk-pos-sentrum-01";

    @Test
    @DisplayName("should_clear_redis_session_and_return_standby_status_when_reset_called")
    void should_clear_redis_session_and_return_standby_status_when_reset_called() {
        // Arrange
        KioskResetSessionRequest request = KioskResetSessionRequest.builder()
                .storeId(storeId)
                .reason(KioskConstants.REASON_IDLE_TIMEOUT)
                .build();

        // Act
        KioskResetSessionResponse response = useCase.execute(tenantId, deviceId, request);

        // Assert
        assertNotNull(response);
        assertEquals(KioskConstants.KIOSK_STATUS_STANDBY, response.getKioskStatus());
        assertEquals(KioskConstants.WATCHDOG_IDLE_TIMEOUT_SECONDS, response.getCountdownDefaultSeconds());
        verify(redisKioskSessionAdapter).clearSession(tenantId, deviceId);
    }

    @Test
    @DisplayName("should_throw_exception_when_tenant_is_null")
    void should_throw_exception_when_tenant_is_null() {
        KioskResetSessionRequest request = KioskResetSessionRequest.builder()
                .storeId(storeId)
                .reason(KioskConstants.REASON_USER_MANUAL_RESET)
                .build();

        assertThrows(NullPointerException.class, () -> useCase.execute(null, deviceId, request));
        verifyNoInteractions(redisKioskSessionAdapter);
    }
}

