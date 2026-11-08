package com.ordera.modules.catalog.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.impl.GetProductDetailUseCaseImpl;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.mapper.CatalogDtoMapper;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierGroupRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.catalog.presentation.dto.ProductDetailResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProductDetailUseCase Unit Tests")
class GetProductDetailUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductModifierGroupRepository modifierGroupRepository;

    @Mock
    private ProductModifierRepository modifierRepository;

    @Spy
    private CatalogDtoMapper catalogDtoMapper = new CatalogDtoMapper() {
        @Override
        public com.ordera.modules.catalog.domain.model.Category toDomain(com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity entity) { return null; }
        @Override
        public com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Category domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.domain.model.Category> toCategoryDomainList(List<com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity> entities) { return null; }
        @Override
        public com.ordera.modules.catalog.presentation.dto.CategoryResponse toCategoryResponse(com.ordera.modules.catalog.domain.model.Category domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.presentation.dto.CategoryResponse> toCategoryResponseList(List<com.ordera.modules.catalog.domain.model.Category> domains) { return null; }
        @Override
        public com.ordera.modules.catalog.domain.model.Subcategory toDomain(com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity entity) { return null; }
        @Override
        public com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Subcategory domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.domain.model.Subcategory> toSubcategoryDomainList(List<com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity> entities) { return null; }
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
        public ProductDetailResponse toProductDetailResponse(com.ordera.modules.catalog.domain.model.Product domain) { return null; }
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
        public com.ordera.modules.catalog.domain.model.Modifier toDomain(ProductModifierJpaEntity entity) { return null; }
        @Override
        public ProductModifierJpaEntity toEntity(com.ordera.modules.catalog.domain.model.Modifier domain) { return null; }
        @Override
        public List<com.ordera.modules.catalog.domain.model.Modifier> toModifierDomainList(List<ProductModifierJpaEntity> entities) { return null; }
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
    private GetProductDetailUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

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
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(productId));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.TENANT_HEADER_MISSING);
    }

    @Test
    @DisplayName("should_throw_PRODUCT_NOT_FOUND_when_product_does_not_exist")
    void should_throw_PRODUCT_NOT_FOUND_when_product_does_not_exist() {
        TenantContext.setTenantId(tenantId);
        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(productId));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("should_return_product_detail_with_modifiers")
    void should_return_product_detail_with_modifiers() {
        // Arrange
        TenantContext.setTenantId(tenantId);

        ProductJpaEntity product = ProductJpaEntity.builder()
                .name("Aurora")
                .description("Trà sữa hoa đậu biếc")
                .basePrice(new BigDecimal("89.00"))
                .originalPrice(new BigDecimal("130.00"))
                .piecesText("1 ly")
                .dietaryTags("[\"Popular\", \"Sweet\"]")
                .imageUrl("https://storage.ordera.no/aurora.jpg")
                .available(true)
                .vatRate(new BigDecimal("15.00"))
                .build();
        product.setId(productId);
        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));

        UUID groupId = UUID.randomUUID();
        ProductModifierGroupJpaEntity group = ProductModifierGroupJpaEntity.builder()
                .productId(productId)
                .name("Size")
                .minSelection(1)
                .maxSelection(1)
                .displayOrder(1)
                .build();
        group.setId(groupId);
        when(modifierGroupRepository.findByProductIdOrderByDisplayOrderAsc(productId)).thenReturn(List.of(group));

        ProductModifierJpaEntity mod1 = ProductModifierJpaEntity.builder()
                .groupId(groupId)
                .name("M")
                .extraPrice(BigDecimal.ZERO)
                .isDefault(false)
                .build();
        mod1.setId(UUID.randomUUID());

        ProductModifierJpaEntity mod2 = ProductModifierJpaEntity.builder()
                .groupId(groupId)
                .name("L")
                .extraPrice(new BigDecimal("15.00"))
                .isDefault(true)
                .build();
        mod2.setId(UUID.randomUUID());

        when(modifierRepository.findByGroupIdIn(List.of(groupId))).thenReturn(List.of(mod1, mod2));

        // Act
        ProductDetailResponse result = useCase.execute(productId);

        // Assert
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.productName()).isEqualTo("Aurora");
        assertThat(result.basePrice()).isEqualByComparingTo(new BigDecimal("89.00"));
        assertThat(result.originalPrice()).isEqualByComparingTo(new BigDecimal("130.00"));
        assertThat(result.piecesText()).isEqualTo("1 ly");
        assertThat(result.dietaryTags()).containsExactly("Popular", "Sweet");
        assertThat(result.modifierGroups()).hasSize(1);
        assertThat(result.modifierGroups().get(0).name()).isEqualTo("Size");
        assertThat(result.modifierGroups().get(0).modifiers()).hasSize(2);
        assertThat(result.modifierGroups().get(0).modifiers().get(1).name()).isEqualTo("L");
        assertThat(result.modifierGroups().get(0).modifiers().get(1).extraPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.modifierGroups().get(0).modifiers().get(1).isDefault()).isTrue();
    }
}
