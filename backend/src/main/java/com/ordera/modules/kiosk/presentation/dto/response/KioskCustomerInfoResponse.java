package com.ordera.modules.kiosk.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskCustomerInfoResponse {

    private boolean isRegistered;
    private UUID userId;
    private String name;
    private String phoneNumber;
    private String currentTier;
    private int currentPoints;
    private int lifetimePoints;
    private double earnRatePercent;
    private int availableVoucherCount;
}

