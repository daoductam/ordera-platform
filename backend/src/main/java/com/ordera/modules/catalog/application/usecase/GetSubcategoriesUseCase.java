package com.ordera.modules.catalog.application.usecase;

import com.ordera.modules.catalog.presentation.dto.SubcategoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Lấy danh sách nhóm con (Subcategory pills) theo danh mục chính.
 */
public interface GetSubcategoriesUseCase {

    List<SubcategoryResponse> execute(UUID categoryId);
}
