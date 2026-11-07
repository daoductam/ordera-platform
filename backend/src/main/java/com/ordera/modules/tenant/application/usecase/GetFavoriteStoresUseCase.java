package com.ordera.modules.tenant.application.usecase;

import com.ordera.modules.tenant.presentation.dto.StoreSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface GetFavoriteStoresUseCase {

    List<StoreSummaryResponse> execute(UUID userId, Double userLat, Double userLng);
}

