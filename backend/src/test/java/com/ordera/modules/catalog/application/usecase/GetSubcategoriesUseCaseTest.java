package com.ordera.modules.catalog.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.impl.GetSubcategoriesUseCaseImpl;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.CategoryRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.SubcategoryRepository;
import com.ordera.modules.catalog.presentation.dto.SubcategoryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSubcategoriesUseCase Unit Tests")
class GetSubcategoriesUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @InjectMocks
    private GetSubcategoriesUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should_throw_TENANT_HEADER_MISSING_when_tenant_is_null")
    void should_throw_TENANT_HEADER_MISSING_when_tenant_is_null() {
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(categoryId));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.TENANT_HEADER_MISSING);
    }

    @Test
    @DisplayName("should_throw_CATEGORY_NOT_FOUND_when_category_not_found_in_tenant")
    void should_throw_CATEGORY_NOT_FOUND_when_category_not_found_in_tenant() {
        TenantContext.setTenantId(tenantId);
        when(categoryRepository.findByIdAndTenantId(categoryId, tenantId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(categoryId));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("should_return_subcategories_sorted_by_display_order")
    void should_return_subcategories_sorted_by_display_order() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        CategoryJpaEntity cat = new CategoryJpaEntity();
        cat.setId(categoryId);
        when(categoryRepository.findByIdAndTenantId(categoryId, tenantId)).thenReturn(Optional.of(cat));

        SubcategoryJpaEntity sub1 = SubcategoryJpaEntity.builder()
                .categoryId(categoryId)
                .name("Fresh maki")
                .displayOrder(1)
                .active(true)
                .build();
        sub1.setId(UUID.randomUUID());

        SubcategoryJpaEntity sub2 = SubcategoryJpaEntity.builder()
                .categoryId(categoryId)
                .name("Fried maki")
                .displayOrder(2)
                .active(true)
                .build();
        sub2.setId(UUID.randomUUID());

        when(subcategoryRepository.findByCategoryIdAndActiveOrderByDisplayOrderAsc(categoryId, true))
                .thenReturn(List.of(sub1, sub2));

        // Act
        List<SubcategoryResponse> result = useCase.execute(categoryId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Fresh maki");
        assertThat(result.get(0).displayOrder()).isEqualTo(1);
        assertThat(result.get(1).name()).isEqualTo("Fried maki");
        assertThat(result.get(1).displayOrder()).isEqualTo(2);
    }
}
