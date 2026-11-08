package com.ordera.modules.reward.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGiftItemResponse {
    private UUID userGiftId;
    private UUID giftId;
    private String title;
    private String description;
    private String imageUrl;
    private String redemptionCode;
    private String status;
    private Instant validUntil;
}
