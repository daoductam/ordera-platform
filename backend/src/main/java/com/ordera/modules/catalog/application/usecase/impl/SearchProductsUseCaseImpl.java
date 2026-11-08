package com.ordera.modules.catalog.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.SearchProductsUseCase;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.mapper.CatalogDtoMapper;
import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.CategoryRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierGroupRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.catalog.presentation.dto.ProductSummaryResponse;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchProductsUseCaseImpl implements SearchProductsUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductModifierGroupRepository modifierGroupRepository;
    private final StoreRepository storeRepository;
    private final CatalogDtoMapper catalogDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> execute(UUID storeId, String query) {
        UUID tenantId = resolveTenantId();
        validateStore(storeId, tenantId);
        validateQuery(query);

        List<ProductJpaEntity> products = searchProducts(tenantId, query.trim());
        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, String> categoryNames = fetchCategoryNames(tenantId);
        Set<UUID> productsWithModifiers = resolveProductsWithModifiers(products);

        return assembleProductSummaries(products, categoryNames, productsWithModifiers);
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

    private void validateQuery(String query) {
        if (query == null || query.trim().length() < 2) {
            throw new BusinessException(CatalogErrorCode.INVALID_QUERY_PARAM,
                    "Search query must contain at least 2 characters.");
        }
    }

    private List<ProductJpaEntity> searchProducts(UUID tenantId, String query) {
        return productRepository.findByTenantIdAndNameContainingIgnoreCaseAndAvailable(tenantId, query, true);
    }

    private Map<UUID, String> fetchCategoryNames(UUID tenantId) {
        return categoryRepository.findByTenantIdOrderByDisplayOrderAsc(tenantId).stream()
                .collect(Collectors.toMap(CategoryJpaEntity::getId, CategoryJpaEntity::getName, (a, b) -> a));
    }

    private Set<UUID> resolveProductsWithModifiers(List<ProductJpaEntity> products) {
        List<UUID> productIds = products.stream().map(ProductJpaEntity::getId).toList();
        List<ProductModifierGroupJpaEntity> groups = modifierGroupRepository.findByProductIdInOrderByDisplayOrderAsc(productIds);
        return groups.stream().map(ProductModifierGroupJpaEntity::getProductId).collect(Collectors.toSet());
    }

    private List<ProductSummaryResponse> assembleProductSummaries(
            List<ProductJpaEntity> products,
            Map<UUID, String> categoryNames,
            Set<UUID> productsWithModifiers) {

        return products.stream().map(p -> {
            List<String> tags = catalogDtoMapper.parseDietaryTags(p.getDietaryTags());
            boolean isPopular = tags.stream().anyMatch(t -> "Popular".equalsIgnoreCase(t) || "POPULAR".equalsIgnoreCase(t));

            return ProductSummaryResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .basePrice(p.getBasePrice())
                    .originalPrice(p.getOriginalPrice())
                    .piecesText(p.getPiecesText())
                    .dietaryTags(tags)
                    .categoryName(categoryNames.getOrDefault(p.getCategoryId(), ""))
                    .imageUrl(p.getImageUrl())
                    .isPopular(isPopular)
                    .isAvailable(p.isAvailable())
                    .hasModifiers(productsWithModifiers.contains(p.getId()))
                    .build();
        }).toList();
    }
}
