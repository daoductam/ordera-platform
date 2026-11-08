package com.ordera.modules.catalog.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.impl.GetProductModifiersUseCaseImpl;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierGroupRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.catalog.presentation.dto.ModifierGroupResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetProductModifiersUseCase Unit Tests")
class GetProductModifiersUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductModifierGroupRepository modifierGroupRepository;

    @Mock
    private ProductModifierRepository modifierRepository;

    @InjectMocks
    private GetProductModifiersUseCaseImpl useCase;

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
    @DisplayName("should_return_modifier_groups_and_options")
    void should_return_modifier_groups_and_options() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        ProductJpaEntity product = new ProductJpaEntity();
        product.setId(productId);
        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));

        UUID groupId = UUID.randomUUID();
        ProductModifierGroupJpaEntity group = ProductModifierGroupJpaEntity.builder()
                .productId(productId)
                .name("Sweetness Level")
                .minSelection(1)
                .maxSelection(1)
                .displayOrder(1)
                .build();
        group.setId(groupId);
        when(modifierGroupRepository.findByProductIdOrderByDisplayOrderAsc(productId)).thenReturn(List.of(group));

        ProductModifierJpaEntity mod1 = ProductModifierJpaEntity.builder()
                .groupId(groupId)
                .name("Standard")
                .extraPrice(BigDecimal.ZERO)
                .isDefault(true)
                .build();
        mod1.setId(UUID.randomUUID());

        ProductModifierJpaEntity mod2 = ProductModifierJpaEntity.builder()
                .groupId(groupId)
                .name("Less Sweet (50%)")
                .extraPrice(BigDecimal.ZERO)
                .isDefault(false)
                .build();
        mod2.setId(UUID.randomUUID());

        when(modifierRepository.findByGroupIdIn(List.of(groupId))).thenReturn(List.of(mod1, mod2));

        // Act
        List<ModifierGroupResponse> result = useCase.execute(productId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Sweetness Level");
        assertThat(result.get(0).modifiers()).hasSize(2);
        assertThat(result.get(0).modifiers().get(0).name()).isEqualTo("Standard");
        assertThat(result.get(0).modifiers().get(0).isDefault()).isTrue();
    }
}
