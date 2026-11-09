package com.ordera.modules.kiosk.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskConfigResponse {

    private String deviceId;
    private String deviceName;
    private KioskStoreConfigDto store;
    private int idleTimeoutSeconds;
    private int successScreenSeconds;
    private boolean printerConnected;
    private String printerPaperStatus;
    private String cardReaderStatus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KioskStoreConfigDto {
        private UUID id;
        private String name;
        private String currency;
        private BigDecimal vatRatePercent;
    }
}

