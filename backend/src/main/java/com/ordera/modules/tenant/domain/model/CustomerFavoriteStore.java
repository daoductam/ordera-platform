package com.ordera.modules.tenant.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model biểu diễn trạng thái yêu thích cửa hàng của người dùng.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFavoriteStore {

    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private UUID storeId;
    private Instant createdAt;
}

