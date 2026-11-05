package com.ordera.modules.tenant.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    private UUID id;
    private UUID tenantId;
    private String name;
    private String address;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phoneNumber;
    private String openingHours;
    private boolean active;
    private boolean acceptsPickup;
    private boolean acceptsDinein;
    private Instant createdAt;
    private Instant updatedAt;
}
