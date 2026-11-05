package com.ordera.modules.reward.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyGift {

    private UUID id;
    private UUID tenantId;
    private String title;
    private String description;
    private String imageUrl;
    private int pointsCost;
    private int stockQuantity;
    private boolean active;
    private Instant createdAt;
}
