package com.ordera.modules.reward.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyGiftRequest {

    @NotNull
    private UUID cartId;

    @NotNull
    private UUID rewardGiftId;
}

