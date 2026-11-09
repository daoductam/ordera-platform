package com.ordera.modules.kiosk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Pure Java Domain Model cho thiết bị Kiosk trạm.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskDevice {

    private UUID id;
    private UUID tenantId;
    private UUID storeId;
    private String deviceCode;
    private String deviceName;
    private String printerStatus;
    private String cardReaderStatus;
    private int idleTimeoutSeconds;
    private boolean active;
    private Instant lastHeartbeatAt;
    private Instant createdAt;
}

