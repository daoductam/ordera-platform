package com.ordera.modules.ledger.infrastructure.persistence.entity;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PointsLedgerId implements Serializable {

    private UUID id;
    private Instant createdAt;
}

