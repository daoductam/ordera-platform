package com.ordera.modules.kiosk.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.kiosk.application.usecase.impl.GetKioskConfigUseCaseImpl;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.infrastructure.mapper.KioskDtoMapper;
import com.ordera.modules.kiosk.infrastructure.persistence.entity.KioskDeviceJpaEntity;
import com.ordera.modules.kiosk.infrastructure.persistence.repository.KioskDeviceJpaRepository;
import com.ordera.modules.kiosk.presentation.dto.response.KioskConfigResponse;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetKioskConfigUseCase Unit Tests")
class GetKioskConfigUseCaseTest {

    @Mock
    private KioskDeviceJpaRepository kioskDeviceRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private KioskDtoMapper kioskDtoMapper;

    @InjectMocks
    private GetKioskConfigUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final String deviceId = "kiosk-pos-sentrum-01";

    @Test
    @DisplayName("should_throw_KIOSK_DEVICE_NOT_FOUND_when_device_does_not_exist")
    void should_throw_KIOSK_DEVICE_NOT_FOUND_when_device_does_not_exist() {
        when(kioskDeviceRepository.findByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> useCase.execute(tenantId, deviceId));
        assertEquals(KioskErrorCode.KIOSK_DEVICE_NOT_FOUND, ex.getErrorCode());
        verifyNoInteractions(storeRepository, kioskDtoMapper);
    }

    @Test
    @DisplayName("should_return_config_and_update_heartbeat_when_device_exists")
    void should_return_config_and_update_heartbeat_when_device_exists() {
        // Arrange
        KioskDeviceJpaEntity device = KioskDeviceJpaEntity.builder()
                .deviceCode(deviceId)
                .deviceName("Counter Touch Terminal 01")
                .storeId(storeId)
                .idleTimeoutSeconds(60)
                .printerStatus("OK")
                .cardReaderStatus("ONLINE")
                .build();
        device.setTenantId(tenantId);

        StoreJpaEntity store = StoreJpaEntity.builder()
                .name("Linn Sushi Sentrum")
                .build();
        store.setId(storeId);
        store.setTenantId(tenantId);

        KioskConfigResponse expectedResponse = KioskConfigResponse.builder()
                .deviceId(deviceId)
                .deviceName("Counter Touch Terminal 01")
                .idleTimeoutSeconds(60)
                .successScreenSeconds(5)
                .printerConnected(true)
                .printerPaperStatus("OK")
                .cardReaderStatus("ONLINE")
                .store(KioskConfigResponse.KioskStoreConfigDto.builder()
                        .id(storeId)
                        .name("Linn Sushi Sentrum")
                        .currency("NOK")
                        .vatRatePercent(new BigDecimal("15.00"))
                        .build())
                .build();

        when(kioskDeviceRepository.findByTenantIdAndDeviceCode(tenantId, deviceId)).thenReturn(Optional.of(device));
        when(kioskDeviceRepository.save(any(KioskDeviceJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));
        when(kioskDtoMapper.toConfigResponse(device, store)).thenReturn(expectedResponse);

        // Act
        KioskConfigResponse response = useCase.execute(tenantId, deviceId);

        // Assert
        assertNotNull(response);
        assertEquals(deviceId, response.getDeviceId());
        assertEquals("Counter Touch Terminal 01", response.getDeviceName());
        assertNotNull(response.getStore());
        assertEquals("Linn Sushi Sentrum", response.getStore().getName());
        verify(kioskDeviceRepository).save(argThat(d -> d.getLastHeartbeatAt() != null));
    }
}

