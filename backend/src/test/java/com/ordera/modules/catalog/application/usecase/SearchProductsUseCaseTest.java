package com.ordera.modules.catalog.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.impl.SearchProductsUseCaseImpl;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.mapper.CatalogDtoMapper;
import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.CategoryRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierGroupRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.catalog.presentation.dto.ProductSummaryResponse;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProductsUseCase Unit Tests")
class SearchProductsUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductModifierGroupRepository modifierGroupRepository;

    @Mock
    private StoreRepository storeRepository;

    @Spy
    private CatalogDtoMapper catalogDtoMapper = new CatalogDtoMapper() {
        @Override public com.ordera.modules.catalog.domain.model.Category toDomain(CategoryJpaEntity entity) { return null; }
        @Override public CategoryJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Category domain) { return null; }
        @Override public List<com.ordera.modules.catalog.domain.model.Category> toCategoryDomainList(List<CategoryJpaEntity> entities) { return null; }
        @Override public com.ordera.modules.catalog.presentation.dto.CategoryResponse toCategoryResponse(com.ordera.modules.catalog.domain.model.Category domain) { return null; }
        @Override public List<com.ordera.modules.catalog.presentation.dto.CategoryResponse> toCategoryResponseList(List<com.ordera.modules.catalog.domain.model.Category> domains) { return null; }
        @Override public com.ordera.modules.catalog.domain.model.Subcategory toDomain(com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity entity) { return null; }
        @Override public com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Subcategory domain) { return null; }
        @Override public List<com.ordera.modules.catalog.domain.model.Subcategory> toSubcategoryDomainList(List<com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity> entities) { return null; }
        @Override public com.ordera.modules.catalog.presentation.dto.SubcategoryResponse toSubcategoryResponse(com.ordera.modules.catalog.domain.model.Subcategory domain) { return null; }
        @Override public List<com.ordera.modules.catalog.presentation.dto.SubcategoryResponse> toSubcategoryResponseList(List<com.ordera.modules.catalog.domain.model.Subcategory> domains) { return null; }
        @Override public com.ordera.modules.catalog.domain.model.Product toDomain(ProductJpaEntity entity) { return null; }
        @Override public ProductJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Product domain) { return null; }
        @Override public List<com.ordera.modules.catalog.domain.model.Product> toProductDomainList(List<ProductJpaEntity> entities) { return null; }
        @Override public ProductSummaryResponse toProductSummaryResponse(com.ordera.modules.catalog.domain.model.Product domain) { return null; }
        @Override public List<ProductSummaryResponse> toProductSummaryResponseList(List<com.ordera.modules.catalog.domain.model.Product> domains) { return null; }
        @Override public com.ordera.modules.catalog.presentation.dto.ProductDetailResponse toProductDetailResponse(com.ordera.modules.catalog.domain.model.Product domain) { return null; }
        @Override public com.ordera.modules.catalog.domain.model.ModifierGroup toDomain(com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity entity) { return null; }
        @Override public com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity toEntity(com.ordera.modules.catalog.domain.model.ModifierGroup domain) { return null; }
        @Override public List<com.ordera.modules.catalog.domain.model.ModifierGroup> toGroupDomainList(List<com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity> entities) { return null; }
        @Override public com.ordera.modules.catalog.presentation.dto.ModifierGroupResponse toModifierGroupResponse(com.ordera.modules.catalog.domain.model.ModifierGroup domain) { return null; }
        @Override public List<com.ordera.modules.catalog.presentation.dto.ModifierGroupResponse> toModifierGroupResponseList(List<com.ordera.modules.catalog.domain.model.ModifierGroup> domains) { return null; }
        @Override public com.ordera.modules.catalog.domain.model.Modifier toDomain(com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity entity) { return null; }
        @Override public com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Modifier domain) { return null; }
        @Override public List<com.ordera.modules.catalog.domain.model.Modifier> toModifierDomainList(List<com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity> entities) { return null; }
        @Override public com.ordera.modules.catalog.presentation.dto.ModifierResponse toModifierResponse(com.ordera.modules.catalog.domain.model.Modifier domain) { return null; }
        @Override public List<com.ordera.modules.catalog.presentation.dto.ModifierResponse> toModifierResponseList(List<com.ordera.modules.catalog.domain.model.Modifier> domains) { return null; }
        @Override public com.ordera.modules.catalog.domain.model.PromotionBanner toDomain(com.ordera.modules.catalog.infrastructure.persistence.entity.PromotionBannerJpaEntity entity) { return null; }
        @Override public com.ordera.modules.catalog.infrastructure.persistence.entity.PromotionBannerJpaEntity toEntity(com.ordera.modules.catalog.domain.model.PromotionBanner domain) { return null; }
        @Override public List<com.ordera.modules.catalog.domain.model.PromotionBanner> toBannerDomainList(List<com.ordera.modules.catalog.infrastructure.persistence.entity.PromotionBannerJpaEntity> entities) { return null; }
        @Override public com.ordera.modules.catalog.presentation.dto.PromotionBannerResponse toBannerResponse(com.ordera.modules.catalog.domain.model.PromotionBanner domain) { return null; }
        @Override public List<com.ordera.modules.catalog.presentation.dto.PromotionBannerResponse> toBannerResponseList(List<com.ordera.modules.catalog.domain.model.PromotionBanner> domains) { return null; }
    };

    @InjectMocks
    private SearchProductsUseCaseImpl useCase;

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
    @DisplayName("should_throw_INVALID_QUERY_PARAM_when_query_is_too_short")
    void should_throw_INVALID_QUERY_PARAM_when_query_is_too_short() {
        TenantContext.setTenantId(tenantId);
        StoreJpaEntity store = new StoreJpaEntity();
        store.setId(storeId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(storeId, "a"));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.INVALID_QUERY_PARAM);
    }

    @Test
    @DisplayName("should_return_matching_products")
    void should_return_matching_products() {
        TenantContext.setTenantId(tenantId);
        StoreJpaEntity store = new StoreJpaEntity();
        store.setId(storeId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));

        UUID catId = UUID.randomUUID();
        CategoryJpaEntity cat = CategoryJpaEntity.builder().name("Maki").build();
        cat.setId(catId);
        when(categoryRepository.findByTenantIdOrderByDisplayOrderAsc(tenantId)).thenReturn(List.of(cat));

        ProductJpaEntity prod = ProductJpaEntity.builder()
                .categoryId(catId)
                .name("Laks Maki")
                .basePrice(new BigDecimal("150.00"))
                .dietaryTags("[\"Popular\"]")
                .available(true)
                .build();
        prod.setId(UUID.randomUUID());

        when(productRepository.findByTenantIdAndNameContainingIgnoreCaseAndAvailable(tenantId, "maki", true))
                .thenReturn(List.of(prod));
        when(modifierGroupRepository.findByProductIdInOrderByDisplayOrderAsc(List.of(prod.getId())))
                .thenReturn(Collections.emptyList());

        List<ProductSummaryResponse> results = useCase.execute(storeId, "maki");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).name()).isEqualTo("Laks Maki");
        assertThat(results.get(0).categoryName()).isEqualTo("Maki");
    }
}
