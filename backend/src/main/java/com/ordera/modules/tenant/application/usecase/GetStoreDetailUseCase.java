package com.ordera.modules.tenant.application.usecase;

import com.ordera.modules.tenant.presentation.dto.StoreDetailResponse;

import java.util.UUID;

public interface GetStoreDetailUseCase {

    StoreDetailResponse execute(UUID storeId);
}

