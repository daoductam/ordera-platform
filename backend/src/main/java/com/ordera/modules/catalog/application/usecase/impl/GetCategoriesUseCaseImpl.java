package com.ordera.modules.catalog.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.GetCategoriesUseCase;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.CategoryRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.SubcategoryRepository;
import com.ordera.modules.catalog.presentation.dto.CategoryResponse;
import com.ordera.modules.catalog.presentation.dto.SubcategoryResponse;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCategoriesUseCaseImpl implements GetCategoriesUseCase {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> execute(UUID storeId) {
        UUID tenantId = resolveTenantId();
        validateStore(storeId, tenantId);

        List<CategoryJpaEntity> categories = fetchCategories(tenantId);
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> categoryIds = categories.stream().map(CategoryJpaEntity::getId).toList();
        Map<UUID, List<SubcategoryResponse>> subcategoriesMap = fetchSubcategoriesMap(categoryIds);

        return assembleCategoryResponses(categories, subcategoriesMap);
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

    private List<CategoryJpaEntity> fetchCategories(UUID tenantId) {
        return categoryRepository.findByTenantIdAndActiveOrderByDisplayOrderAsc(tenantId, true);
    }

    private Map<UUID, List<SubcategoryResponse>> fetchSubcategoriesMap(List<UUID> categoryIds) {
        List<SubcategoryJpaEntity> subcategories =
                subcategoryRepository.findByCategoryIdInAndActiveOrderByDisplayOrderAsc(categoryIds, true);

        return subcategories.stream()
                .collect(Collectors.groupingBy(
                        SubcategoryJpaEntity::getCategoryId,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                sub -> SubcategoryResponse.builder()
                                        .id(sub.getId())
                                        .name(sub.getName())
                                        .displayOrder(sub.getDisplayOrder())
                                        .build(),
                                Collectors.toList()
                        )
                ));
    }

    private List<CategoryResponse> assembleCategoryResponses(
            List<CategoryJpaEntity> categories,
            Map<UUID, List<SubcategoryResponse>> subcategoriesMap) {

        return categories.stream()
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .displayOrder(cat.getDisplayOrder())
                        .icon(cat.getIcon())
                        .subcategories(subcategoriesMap.getOrDefault(cat.getId(), Collections.emptyList()))
                        .build())
                .toList();
    }
}
