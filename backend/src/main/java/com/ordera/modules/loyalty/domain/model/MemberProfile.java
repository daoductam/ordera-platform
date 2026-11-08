package com.ordera.modules.loyalty.domain.model;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfile {

    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private UUID currentTierId;
    private int currentPoints;
    private int lifetimePoints;
    private String barcodeToken;
    private LocalDate dateOfBirth;
    private String gender;
    private String avatarUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
