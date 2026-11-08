package com.ordera.modules.catalog.application.usecase;

import com.ordera.modules.catalog.presentation.dto.StoreMenuResponse;

import java.util.UUID;

/**
 * Trả về cây thực đơn phân cấp hoàn chỉnh (Categories ➜ Subcategories ➜ Products).
 */
public interface GetStoreMenuUseCase {

    StoreMenuResponse execute(UUID storeId);
}
