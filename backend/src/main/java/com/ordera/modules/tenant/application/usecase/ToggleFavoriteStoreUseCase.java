package com.ordera.modules.tenant.application.usecase;

import com.ordera.modules.tenant.presentation.dto.ToggleFavoriteResponse;

import java.util.UUID;

public interface ToggleFavoriteStoreUseCase {

    ToggleFavoriteResponse execute(UUID storeId, UUID userId, boolean favorite);
}

