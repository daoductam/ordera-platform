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
public class ResolvedTableQrResponse {

    private UUID storeId;
    private String storeName;
    private UUID tableId;
    private String tableNumber;
    private String zone;
    private int capacity;
    private boolean isOccupied;
}

