package com.ordera.modules.tenant.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreTable {

    private UUID id;
    private UUID tenantId;
    private UUID storeId;
    private String tableNumber;
    private int seatingCapacity;
    private String qrCodeToken;
    private boolean occupied;
    private Instant createdAt;
}

