package com.ordera.modules.reward.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogGiftItemResponse {
    private UUID id;
    private String title;
    private String description;
    private String imageUrl;
    private int pointsCost;
    private int stockQuantity;
    private boolean canAfford;
    private int pointsNeeded;
}
