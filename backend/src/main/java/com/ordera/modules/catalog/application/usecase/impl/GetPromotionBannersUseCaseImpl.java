package com.ordera.modules.catalog.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.GetPromotionBannersUseCase;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.persistence.entity.PromotionBannerJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.PromotionBannerRepository;
import com.ordera.modules.catalog.presentation.dto.PromotionBannerResponse;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPromotionBannersUseCaseImpl implements GetPromotionBannersUseCase {

    private final PromotionBannerRepository promotionBannerRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PromotionBannerResponse> execute(UUID storeId) {
        UUID tenantId = resolveTenantId();
        validateStore(storeId, tenantId);

        List<PromotionBannerJpaEntity> banners = fetchBanners(tenantId, storeId);
        return assembleBannerResponses(banners);
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(CatalogErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private void validateStore(UUID storeId, UUID tenantId) {
        if (storeId == null) {
            throw new BusinessException(CatalogErrorCode.STORE_NOT_FOUND, "Store ID cannot be null.");
        }
        storeRepository.findByIdAndTenantId(storeId, tenantId)
                .orElseThrow(() -> new BusinessException(CatalogErrorCode.STORE_NOT_FOUND,
                        "Store with ID '" + storeId + "' does not exist."));
    }

    private List<PromotionBannerJpaEntity> fetchBanners(UUID tenantId, UUID storeId) {
        return promotionBannerRepository.findActiveBanners(tenantId, storeId);
    }

    private List<PromotionBannerResponse> assembleBannerResponses(List<PromotionBannerJpaEntity> banners) {
        if (banners == null || banners.isEmpty()) {
            return Collections.emptyList();
        }
        return banners.stream()
                .map(b -> PromotionBannerResponse.builder()
                        .id(b.getId())
                        .title(b.getTitle())
                        .subtitle(b.getSubtitle())
                        .imageUrl(b.getImageUrl())
                        .ctaLabel(b.getCtaLabel())
                        .ctaActionType(b.getActionType())
                        .targetCategoryId(b.getTargetId())
                        .active(b.isActive())
                        .displayOrder(b.getDisplayOrder())
                        .build())
                .toList();
    }
}
