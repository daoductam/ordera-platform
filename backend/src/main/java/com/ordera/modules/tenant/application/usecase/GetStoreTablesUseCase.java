package com.ordera.modules.tenant.application.usecase;

import com.ordera.modules.tenant.presentation.dto.StoreTableResponse;

import java.util.List;
import java.util.UUID;

public interface GetStoreTablesUseCase {

    List<StoreTableResponse> execute(UUID storeId);
}

