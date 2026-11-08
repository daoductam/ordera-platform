package com.ordera.modules.catalog.application.usecase;

import com.ordera.modules.catalog.presentation.dto.ProductSummaryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Tìm kiếm món ăn theo từ khóa tại chi nhánh.
 */
public interface SearchProductsUseCase {

    List<ProductSummaryResponse> execute(UUID storeId, String query);
}
