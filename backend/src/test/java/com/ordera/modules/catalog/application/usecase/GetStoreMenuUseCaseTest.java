package com.ordera.modules.catalog.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.impl.GetStoreMenuUseCaseImpl;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.mapper.CatalogDtoMapper;
import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.CategoryRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierGroupRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.SubcategoryRepository;
import com.ordera.modules.catalog.presentation.dto.StoreMenuResponse;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStoreMenuUseCase Unit Tests")
class GetStoreMenuUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductModifierGroupRepository modifierGroupRepository;

    @Mock
    private StoreRepository storeRepository;

    @Spy
    private CatalogDtoMapper catalogDtoMapper = new CatalogDtoMapper() {
        @Override
        public com.ordera.modules.catalog.domain.model.Category toDomain(CategoryJpaEntity entity) { return null; }
        @Override
        public CategoryJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Category domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.domain.model.Category> toCategoryDomainList(List<CategoryJpaEntity> entities) { return null; }
        @Override
        public com.ordera.modules.catalog.presentation.dto.CategoryResponse toCategoryResponse(com.ordera.modules.catalog.domain.model.Category domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.presentation.dto.CategoryResponse> toCategoryResponseList(List<com.ordera.modules.catalog.domain.model.Category> domains) { return null; }
        @Override
        public com.ordera.modules.catalog.domain.model.Subcategory toDomain(SubcategoryJpaEntity entity) { return null; }
        @Override
        public SubcategoryJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Subcategory domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.domain.model.Subcategory> toSubcategoryDomainList(List<SubcategoryJpaEntity> entities) { return null; }
        @Override
        public com.ordera.modules.catalog.presentation.dto.SubcategoryResponse toSubcategoryResponse(com.ordera.modules.catalog.domain.model.Subcategory domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.presentation.dto.SubcategoryResponse> toSubcategoryResponseList(List<com.ordera.modules.catalog.domain.model.Subcategory> domains) { return null; }
        @Override
        public com.ordera.modules.catalog.domain.model.Product toDomain(ProductJpaEntity entity) { return null; }
        @Override
        public ProductJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Product domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.domain.model.Product> toProductDomainList(List<ProductJpaEntity> entities) { return null; }
        @Override
        public com.ordera.modules.catalog.presentation.dto.ProductSummaryResponse toProductSummaryResponse(com.ordera.modules.catalog.domain.model.Product domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.presentation.dto.ProductSummaryResponse> toProductSummaryResponseList(List<com.ordera.modules.catalog.domain.model.Product> domains) { return null; }
        @Override
        public com.ordera.modules.catalog.presentation.dto.ProductDetailResponse toProductDetailResponse(com.ordera.modules.catalog.domain.model.Product domain) { return null; }
        @Override
        public com.ordera.modules.catalog.domain.model.ModifierGroup toDomain(ProductModifierGroupJpaEntity entity) { return null; }
        @Override
        public ProductModifierGroupJpaEntity toEntity(com.ordera.modules.catalog.domain.model.ModifierGroup domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.domain.model.ModifierGroup> toGroupDomainList(List<ProductModifierGroupJpaEntity> entities) { return null; }
        @Override
        public com.ordera.modules.catalog.presentation.dto.ModifierGroupResponse toModifierGroupResponse(com.ordera.modules.catalog.domain.model.ModifierGroup domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.presentation.dto.ModifierGroupResponse> toModifierGroupResponseList(List<com.ordera.modules.catalog.domain.model.ModifierGroup> domains) { return null; }
        @Override
        public com.ordera.modules.catalog.domain.model.Modifier toDomain(com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity entity) { return null; }
        @Override
        public com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Modifier domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.domain.model.Modifier> toModifierDomainList(List<com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity> entities) { return null; }
        @Override
        public com.ordera.modules.catalog.presentation.dto.ModifierResponse toModifierResponse(com.ordera.modules.catalog.domain.model.Modifier domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.presentation.dto.ModifierResponse> toModifierResponseList(List<com.ordera.modules.catalog.domain.model.Modifier> domains) { return null; }
        @Override
        public com.ordera.modules.catalog.domain.model.PromotionBanner toDomain(com.ordera.modules.catalog.infrastructure.persistence.entity.PromotionBannerJpaEntity entity) { return null; }
        @Override
        public com.ordera.modules.catalog.infrastructure.persistence.entity.PromotionBannerJpaEntity toEntity(com.ordera.modules.catalog.domain.model.PromotionBanner domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.domain.model.PromotionBanner> toBannerDomainList(List<com.ordera.modules.catalog.infrastructure.persistence.entity.PromotionBannerJpaEntity> entities) { return null; }
        @Override
        public com.ordera.modules.catalog.presentation.dto.PromotionBannerResponse toBannerResponse(com.ordera.modules.catalog.domain.model.PromotionBanner domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.presentation.dto.PromotionBannerResponse> toBannerResponseList(List<com.ordera.modules.catalog.domain.model.PromotionBanner> domains) { return null; }
    };

    @InjectMocks
    private GetStoreMenuUseCaseImpl useCase;

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
    @DisplayName("should_throw_STORE_NOT_FOUND_when_store_not_in_tenant")
    void should_throw_STORE_NOT_FOUND_when_store_not_in_tenant() {
        TenantContext.setTenantId(tenantId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(storeId));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("should_build_hierarchical_menu_tree_with_subcategories_and_products")
    void should_build_hierarchical_menu_tree_with_subcategories_and_products() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        StoreJpaEntity store = new StoreJpaEntity();
        store.setId(storeId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));

        UUID catId = UUID.randomUUID();
        CategoryJpaEntity cat = CategoryJpaEntity.builder()
                .name("Maki")
                .icon("sushi")
                .displayOrder(1)
                .active(true)
                .build();
        cat.setId(catId);
        when(categoryRepository.findByTenantIdAndActiveOrderByDisplayOrderAsc(tenantId, true))
                .thenReturn(List.of(cat));

        UUID subId = UUID.randomUUID();
        SubcategoryJpaEntity sub = SubcategoryJpaEntity.builder()
                .categoryId(catId)
                .name("Fresh maki")
                .displayOrder(1)
                .active(true)
                .build();
        sub.setId(subId);
        when(subcategoryRepository.findByCategoryIdInAndActiveOrderByDisplayOrderAsc(List.of(catId), true))
                .thenReturn(List.of(sub));

        UUID prodId = UUID.randomUUID();
        ProductJpaEntity prod = ProductJpaEntity.builder()
                .categoryId(catId)
                .subcategoryId(subId)
                .name("Laks Maki")
                .description("8 miếng cá hồi tươi")
                .basePrice(new BigDecimal("150.00"))
                .originalPrice(new BigDecimal("180.00"))
                .piecesText("8 biter")
                .dietaryTags("[\"Popular\", \"Gluten Free\"]")
                .imageUrl("https://storage.ordera.no/laks.jpg")
                .available(true)
                .vatRate(new BigDecimal("15.00"))
                .build();
        prod.setId(prodId);

        when(productRepository.findByTenantIdAndAvailableOrderByCreatedAtDesc(tenantId, true))
                .thenReturn(List.of(prod));
        when(productRepository.findByTenantId(tenantId)).thenReturn(List.of(prod));

        ProductModifierGroupJpaEntity modGroup = ProductModifierGroupJpaEntity.builder()
                .productId(prodId)
                .name("Size")
                .minSelection(1)
                .maxSelection(1)
                .build();
        when(modifierGroupRepository.findByProductIdInOrderByDisplayOrderAsc(List.of(prodId)))
                .thenReturn(List.of(modGroup));

        // Act
        StoreMenuResponse result = useCase.execute(storeId);

        // Assert
        assertThat(result.storeId()).isEqualTo(storeId);
        assertThat(result.categories()).hasSize(1);
        assertThat(result.categories().get(0).categoryName()).isEqualTo("Maki");
        assertThat(result.categories().get(0).subcategories()).hasSize(1);
        assertThat(result.categories().get(0).subcategories().get(0).subcategoryName()).isEqualTo("Fresh maki");

        var products = result.categories().get(0).subcategories().get(0).products();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).name()).isEqualTo("Laks Maki");
        assertThat(products.get(0).basePrice()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(products.get(0).originalPrice()).isEqualByComparingTo(new BigDecimal("180.00"));
        assertThat(products.get(0).piecesText()).isEqualTo("8 biter");
        assertThat(products.get(0).isPopular()).isTrue();
        assertThat(products.get(0).hasModifiers()).isTrue();
    }
}
