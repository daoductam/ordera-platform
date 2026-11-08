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
public class CartOfferGiftItemResponse {
    private UUID giftId;
    private String title;
    private String imageUrl;
    private boolean isApplicable;
    private String status;
}

