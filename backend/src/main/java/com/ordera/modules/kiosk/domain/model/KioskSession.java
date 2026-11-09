package com.ordera.modules.kiosk.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Model đại diện cho phiên làm việc tạm thời trên Kiosk Touch Terminal.
 * Được lưu trữ có thời hạn (TTL 30 phút) trên Redis Cache.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID tenantId;
    private UUID storeId;
    private String deviceCode;
    private UUID customerId;
    private String customerPhone;
    private String customerName;
    private String currentTier;
    private int currentPoints;
    private double earnRatePercent;
    private boolean registered;
    private String status;
    private Instant createdAt;
    private Instant lastActivityAt;
}

