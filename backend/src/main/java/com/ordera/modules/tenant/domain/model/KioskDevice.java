package com.ordera.modules.tenant.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model biểu diễn thiết bị Kiosk POS đặt tại quầy điểm bán.
 */
@Getter
@Setter
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

