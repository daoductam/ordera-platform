package com.ordera.modules.tenant.application.usecase;

import com.ordera.modules.tenant.domain.model.FulfillmentType;
import com.ordera.modules.tenant.presentation.dto.StoreSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface GetStoresListUseCase {

    List<StoreSummaryResponse> execute(String keyword, FulfillmentType fulfillmentType, Double userLat, Double userLng, UUID currentUserId);
}

