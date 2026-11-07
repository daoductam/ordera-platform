package com.ordera.modules.tenant.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreTableResponse {

    private UUID id;
    private UUID storeId;
    private String tableNumber;
    private String zone;
    private int seatingCapacity;
    private String qrCodeToken;
    private boolean isOccupied;
}

