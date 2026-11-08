package com.ordera.modules.catalog.application.usecase;

import com.ordera.modules.catalog.presentation.dto.CategoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Lấy danh sách các nhóm danh mục chính kèm subcategories theo thứ tự hiển thị display_order và chi nhánh.
 */
public interface GetCategoriesUseCase {

    List<CategoryResponse> execute(UUID storeId);
}
