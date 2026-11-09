package com.ordera.modules.kiosk.infrastructure.mapper;

import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.domain.model.KioskDevice;
import com.ordera.modules.kiosk.infrastructure.persistence.entity.KioskDeviceJpaEntity;
import com.ordera.modules.kiosk.presentation.dto.response.KioskConfigResponse;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface KioskDtoMapper {

    KioskDevice toDomain(KioskDeviceJpaEntity entity);

    KioskDeviceJpaEntity toEntity(KioskDevice domain);

    List<KioskDevice> toDomainList(List<KioskDeviceJpaEntity> entities);

    default KioskConfigResponse toConfigResponse(KioskDeviceJpaEntity device, StoreJpaEntity store) {
        if (device == null) {
            return null;
        }

        KioskConfigResponse.KioskStoreConfigDto storeDto = null;
        if (store != null) {
            storeDto = KioskConfigResponse.KioskStoreConfigDto.builder()
                    .id(store.getId())
                    .name(store.getName())
                    .currency(KioskConstants.DEFAULT_CURRENCY)
                    .vatRatePercent(new BigDecimal("15.00"))
                    .build();
        }

        return KioskConfigResponse.builder()
                .deviceId(device.getDeviceCode())
                .deviceName(device.getDeviceName())
                .store(storeDto)
                .idleTimeoutSeconds(device.getIdleTimeoutSeconds() > 0 ? device.getIdleTimeoutSeconds() : KioskConstants.WATCHDOG_IDLE_TIMEOUT_SECONDS)
                .successScreenSeconds(KioskConstants.POST_PAYMENT_REDIRECT_SECONDS)
                .printerConnected(true)
                .printerPaperStatus(device.getPrinterStatus() != null ? device.getPrinterStatus() : KioskConstants.PRINTER_STATUS_OK)
                .cardReaderStatus(device.getCardReaderStatus() != null ? device.getCardReaderStatus() : KioskConstants.CARD_READER_STATUS_ONLINE)
                .build();
    }
}
