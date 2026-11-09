package com.ordera.modules.kiosk.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.kiosk.application.usecase.GetKioskConfigUseCase;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.infrastructure.mapper.KioskDtoMapper;
import com.ordera.modules.kiosk.infrastructure.persistence.entity.KioskDeviceJpaEntity;
import com.ordera.modules.kiosk.infrastructure.persistence.repository.KioskDeviceJpaRepository;
import com.ordera.modules.kiosk.presentation.dto.response.KioskConfigResponse;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Orchestrator UseCase: Cung cấp thông tin cấu hình trạm Kiosk và ghi nhận tín hiệu heartbeat.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetKioskConfigUseCaseImpl implements GetKioskConfigUseCase {

    private final KioskDeviceJpaRepository kioskDeviceRepository;
    private final StoreRepository storeRepository;
    private final KioskDtoMapper kioskDtoMapper;

    @Override
    @Transactional
    public KioskConfigResponse execute(UUID tenantId, String deviceId) {
        validateInputs(tenantId, deviceId);
        KioskDeviceJpaEntity device = findAndHeartbeatDevice(tenantId, deviceId);
        StoreJpaEntity store = findStore(tenantId, device.getStoreId());

        return kioskDtoMapper.toConfigResponse(device, store);
    }

    private void validateInputs(UUID tenantId, String deviceId) {
        Objects.requireNonNull(tenantId, "TenantId must not be null");
        Objects.requireNonNull(deviceId, "DeviceId must not be null");
    }

    private KioskDeviceJpaEntity findAndHeartbeatDevice(UUID tenantId, String deviceId) {
        KioskDeviceJpaEntity device = kioskDeviceRepository.findByTenantIdAndDeviceCode(tenantId, deviceId)
                .orElseThrow(() -> new BusinessException(KioskErrorCode.KIOSK_DEVICE_NOT_FOUND));

        device.setLastHeartbeatAt(Instant.now());
        return kioskDeviceRepository.save(device);
    }

    private StoreJpaEntity findStore(UUID tenantId, UUID storeId) {
        return storeRepository.findByIdAndTenantId(storeId, tenantId).orElse(null);
    }
}

