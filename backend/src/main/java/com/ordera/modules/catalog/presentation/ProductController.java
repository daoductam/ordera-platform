package com.ordera.modules.catalog.presentation;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.catalog.application.usecase.GetProductDetailUseCase;
import com.ordera.modules.catalog.application.usecase.GetProductModifiersUseCase;
import com.ordera.modules.catalog.application.usecase.GetStoreMenuUseCase;
import com.ordera.modules.catalog.application.usecase.SearchProductsUseCase;
import com.ordera.modules.catalog.presentation.constant.CatalogMessages;
import com.ordera.modules.catalog.presentation.dto.ModifierGroupResponse;
import com.ordera.modules.catalog.presentation.dto.ProductDetailResponse;
import com.ordera.modules.catalog.presentation.dto.ProductSummaryResponse;
import com.ordera.modules.catalog.presentation.dto.StoreMenuResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "02. Catalog - Products", description = "Quản lý cây thực đơn phân cấp, chi tiết món ăn & tùy biến Modifiers")
public class ProductController {

    private final GetStoreMenuUseCase getStoreMenuUseCase;
    private final GetProductDetailUseCase getProductDetailUseCase;
    private final GetProductModifiersUseCase getProductModifiersUseCase;
    private final SearchProductsUseCase searchProductsUseCase;

    @Operation(summary = "Cây thực đơn đầy đủ theo chi nhánh", description = "Lấy toàn bộ cây danh mục, nhóm con và danh sách món ăn phân theo từng section")
    @GetMapping("/api/v1/stores/{storeId}/menu")
    public ResponseEntity<ApiResponse<StoreMenuResponse>> getStoreMenu(
            @PathVariable UUID storeId) {

        StoreMenuResponse menu = getStoreMenuUseCase.execute(storeId);
        return ResponseEntity.ok(ApiResponse.ok(CatalogMessages.MSG_STORE_MENU_RETRIEVED, menu));
    }

    @Operation(summary = "Chi tiết một món ăn", description = "Lấy chi tiết món ăn kèm originalPrice, piecesText, dietaryTags và các nhóm modifiers")
    @GetMapping("/api/v1/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductDetail(
            @PathVariable UUID productId) {

        ProductDetailResponse product = getProductDetailUseCase.execute(productId);
        return ResponseEntity.ok(ApiResponse.ok(CatalogMessages.MSG_PRODUCT_DETAILS_RETRIEVED, product));
    }

    @Operation(summary = "Nhóm tùy chọn Modifiers của món", description = "Lấy danh sách các nhóm tùy chọn (Size, Toppings, Mức đá, Mức đường) của món ăn")
    @GetMapping("/api/v1/products/{productId}/modifiers")
    public ResponseEntity<ApiResponse<List<ModifierGroupResponse>>> getProductModifiers(
            @PathVariable UUID productId) {

        List<ModifierGroupResponse> modifiers = getProductModifiersUseCase.execute(productId);
        return ResponseEntity.ok(ApiResponse.ok(CatalogMessages.MSG_PRODUCT_MODIFIERS_RETRIEVED, modifiers));
    }

    @Operation(summary = "Tìm kiếm món ăn", description = "Tìm kiếm món ăn theo từ khóa tại chi nhánh")
    @GetMapping("/api/v1/stores/{storeId}/products/search")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> searchProducts(
            @PathVariable UUID storeId,
            @RequestParam String query) {

        List<ProductSummaryResponse> products = searchProductsUseCase.execute(storeId, query);
        return ResponseEntity.ok(ApiResponse.ok(CatalogMessages.MSG_SEARCH_COMPLETED, products));
    }
}
