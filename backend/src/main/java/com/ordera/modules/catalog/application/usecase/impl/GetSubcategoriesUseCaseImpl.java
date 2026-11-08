package com.ordera.modules.catalog.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.GetSubcategoriesUseCase;
import com.ordera.modules.catalog.domain.constant.CatalogConstants;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.CategoryRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.SubcategoryRepository;
import com.ordera.modules.catalog.presentation.dto.SubcategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSubcategoriesUseCaseImpl implements GetSubcategoriesUseCase {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SubcategoryResponse> execute(UUID categoryId) {
        UUID tenantId = resolveTenantId();
        validateCategory(categoryId, tenantId);

        List<SubcategoryJpaEntity> subcategories = fetchSubcategories(categoryId);
        return assembleSubcategoryResponses(subcategories);
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(CatalogErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private void validateCategory(UUID categoryId, UUID tenantId) {
        if (categoryId == null) {
            throw new BusinessException(CatalogErrorCode.CATEGORY_NOT_FOUND, CatalogConstants.ERR_CATEGORY_ID_REQUIRED);
        }
        categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new BusinessException(CatalogErrorCode.CATEGORY_NOT_FOUND,
                        "Category with ID '" + categoryId + "' does not exist in this tenant."));
    }

    private List<SubcategoryJpaEntity> fetchSubcategories(UUID categoryId) {
        return subcategoryRepository.findByCategoryIdAndActiveOrderByDisplayOrderAsc(categoryId, true);
    }

    private List<SubcategoryResponse> assembleSubcategoryResponses(List<SubcategoryJpaEntity> subcategories) {
        return subcategories.stream()
                .map(sub -> SubcategoryResponse.builder()
                        .id(sub.getId())
                        .name(sub.getName())
                        .displayOrder(sub.getDisplayOrder())
                        .build())
                .toList();
    }
}
