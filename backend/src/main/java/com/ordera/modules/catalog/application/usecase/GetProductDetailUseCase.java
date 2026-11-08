package com.ordera.modules.catalog.application.usecase;

import com.ordera.modules.catalog.presentation.dto.ProductDetailResponse;

import java.util.UUID;

/**
 * Lấy chi tiết món kèm originalPrice, piecesText, dietaryTags và các nhóm modifier (bắt buộc/tùy chọn).
 */
public interface GetProductDetailUseCase {

    ProductDetailResponse execute(UUID productId);
}
