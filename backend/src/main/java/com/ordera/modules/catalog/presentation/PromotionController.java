package com.ordera.modules.catalog.presentation;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.catalog.application.usecase.GetPromotionBannersUseCase;
import com.ordera.modules.catalog.presentation.constant.CatalogMessages;
import com.ordera.modules.catalog.presentation.dto.PromotionBannerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "02. Catalog - Promotions", description = "Quản lý banner tiếp thị & chiến dịch khuyến mãi")
public class PromotionController {

    private final GetPromotionBannersUseCase getPromotionBannersUseCase;

    @Operation(summary = "Danh sách banner khuyến mãi theo chi nhánh", description = "Lấy danh sách banner chiến dịch tiếp thị đang kích hoạt cho chi nhánh hoặc toàn hệ thống")
    @GetMapping("/api/v1/stores/{storeId}/promotions/banners")
    public ResponseEntity<ApiResponse<List<PromotionBannerResponse>>> getPromotionBanners(
            @PathVariable UUID storeId) {

        List<PromotionBannerResponse> banners = getPromotionBannersUseCase.execute(storeId);
        return ResponseEntity.ok(ApiResponse.ok(CatalogMessages.MSG_PROMOTION_BANNERS_RETRIEVED, banners));
    }
}
