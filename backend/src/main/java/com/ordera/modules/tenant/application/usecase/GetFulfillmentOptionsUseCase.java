package com.ordera.modules.tenant.application.usecase;

import com.ordera.modules.tenant.presentation.dto.FulfillmentOptionsResponse;

import java.util.UUID;

public interface GetFulfillmentOptionsUseCase {
    FulfillmentOptionsResponse execute(UUID storeId);
}

