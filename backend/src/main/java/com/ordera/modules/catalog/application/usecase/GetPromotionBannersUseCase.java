package com.ordera.modules.catalog.application.usecase;

import com.ordera.modules.catalog.presentation.dto.PromotionBannerResponse;

import java.util.List;
import java.util.UUID;

/**
 * Lấy danh sách banner quảng cáo đang kích hoạt (Hot Deals, Seasonal) của chi nhánh hoặc toàn hệ thống.
 */
public interface GetPromotionBannersUseCase {

    List<PromotionBannerResponse> execute(UUID storeId);
}
