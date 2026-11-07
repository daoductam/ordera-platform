package com.ordera.modules.tenant.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleFavoriteResponse {

    private UUID storeId;
    private boolean isFavorite;
}

