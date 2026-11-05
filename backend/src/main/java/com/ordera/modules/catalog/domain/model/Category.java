package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private UUID id;
    private UUID tenantId;
    private String name;
    private int displayOrder;
    private boolean active;
    private Instant createdAt;
}
