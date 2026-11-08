package com.ordera.modules.catalog.presentation;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.catalog.application.usecase.GetCategoriesUseCase;
import com.ordera.modules.catalog.application.usecase.GetSubcategoriesUseCase;
import com.ordera.modules.catalog.presentation.constant.CatalogMessages;
import com.ordera.modules.catalog.presentation.dto.CategoryResponse;
import com.ordera.modules.catalog.presentation.dto.SubcategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "02. Catalog - Categories", description = "Quản lý danh mục món ăn & nhóm con (Subcategory pills)")
public class CategoryController {

    private final GetCategoriesUseCase getCategoriesUseCase;
    private final GetSubcategoriesUseCase getSubcategoriesUseCase;

    @Operation(summary = "Danh sách danh mục theo chi nhánh", description = "Lấy danh sách danh mục chính kèm subcategories dạng pills theo chi nhánh")
    @GetMapping("/api/v1/stores/{storeId}/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesByStore(
            @PathVariable UUID storeId) {

        List<CategoryResponse> categories = getCategoriesUseCase.execute(storeId);
        return ResponseEntity.ok(ApiResponse.ok(CatalogMessages.MSG_CATEGORIES_RETRIEVED, categories));
    }

    @Operation(summary = "Danh sách nhóm con theo danh mục", description = "Lấy danh sách các subcategory pills thuộc một danh mục chính")
    @GetMapping("/api/v1/categories/{categoryId}/subcategories")
    public ResponseEntity<ApiResponse<List<SubcategoryResponse>>> getSubcategoriesByCategory(
            @PathVariable UUID categoryId) {

        List<SubcategoryResponse> subcategories = getSubcategoriesUseCase.execute(categoryId);
        return ResponseEntity.ok(ApiResponse.ok(CatalogMessages.MSG_SUBCATEGORIES_RETRIEVED, subcategories));
    }
}
