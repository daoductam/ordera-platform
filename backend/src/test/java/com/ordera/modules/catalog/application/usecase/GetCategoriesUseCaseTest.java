package com.ordera.modules.catalog.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.impl.GetCategoriesUseCaseImpl;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.CategoryRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.SubcategoryRepository;
import com.ordera.modules.catalog.presentation.dto.CategoryResponse;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCategoriesUseCase Unit Tests")
class GetCategoriesUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private GetCategoriesUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should_throw_TENANT_HEADER_MISSING_when_tenantContext_is_empty")
    void should_throw_TENANT_HEADER_MISSING_when_tenantContext_is_empty() {
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(storeId));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.TENANT_HEADER_MISSING);
    }

    @Test
    @DisplayName("should_throw_STORE_NOT_FOUND_when_store_does_not_exist_in_tenant")
    void should_throw_STORE_NOT_FOUND_when_store_does_not_exist_in_tenant() {
        TenantContext.setTenantId(tenantId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(storeId));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("should_return_categories_with_nested_subcategories_sorted_by_display_order")
    void should_return_categories_with_nested_subcategories_sorted_by_display_order() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        StoreJpaEntity store = new StoreJpaEntity();
        store.setId(storeId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));

        UUID cat1Id = UUID.randomUUID();
        UUID cat2Id = UUID.randomUUID();

        CategoryJpaEntity cat1 = CategoryJpaEntity.builder()
                .name("Maki")
                .icon("sushi")
                .displayOrder(1)
                .active(true)
                .build();
        cat1.setId(cat1Id);

        CategoryJpaEntity cat2 = CategoryJpaEntity.builder()
                .name("Drinks")
                .icon("cup-soda")
                .displayOrder(2)
                .active(true)
                .build();
        cat2.setId(cat2Id);

        when(categoryRepository.findByTenantIdAndActiveOrderByDisplayOrderAsc(tenantId, true))
                .thenReturn(List.of(cat1, cat2));

        SubcategoryJpaEntity sub1 = SubcategoryJpaEntity.builder()
                .categoryId(cat1Id)
                .name("Fresh maki")
                .displayOrder(1)
                .active(true)
                .build();
        sub1.setId(UUID.randomUUID());

        SubcategoryJpaEntity sub2 = SubcategoryJpaEntity.builder()
                .categoryId(cat1Id)
                .name("Fried maki")
                .displayOrder(2)
                .active(true)
                .build();
        sub2.setId(UUID.randomUUID());

        when(subcategoryRepository.findByCategoryIdInAndActiveOrderByDisplayOrderAsc(List.of(cat1Id, cat2Id), true))
                .thenReturn(List.of(sub1, sub2));

        // Act
        List<CategoryResponse> result = useCase.execute(storeId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Maki");
        assertThat(result.get(0).icon()).isEqualTo("sushi");
        assertThat(result.get(0).subcategories()).hasSize(2);
        assertThat(result.get(0).subcategories().get(0).name()).isEqualTo("Fresh maki");
        assertThat(result.get(0).subcategories().get(1).name()).isEqualTo("Fried maki");

        assertThat(result.get(1).name()).isEqualTo("Drinks");
        assertThat(result.get(1).subcategories()).isEmpty();
    }
}
