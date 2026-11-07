package com.ordera.modules.tenant.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model đại diện cho bàn ăn tại chi nhánh cửa hàng.
 */
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
    private String zone;
    private int seatingCapacity;
    private String qrCodeToken;
    private boolean occupied;
    private Instant createdAt;
}
