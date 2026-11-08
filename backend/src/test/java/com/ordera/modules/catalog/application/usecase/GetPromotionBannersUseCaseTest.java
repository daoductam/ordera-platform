package com.ordera.modules.catalog.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.impl.GetPromotionBannersUseCaseImpl;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.persistence.entity.PromotionBannerJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.PromotionBannerRepository;
import com.ordera.modules.catalog.presentation.dto.PromotionBannerResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPromotionBannersUseCase Unit Tests")
class GetPromotionBannersUseCaseTest {

    @Mock
    private PromotionBannerRepository promotionBannerRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private GetPromotionBannersUseCaseImpl useCase;

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
    @DisplayName("should_throw_TENANT_HEADER_MISSING_when_tenant_is_null")
    void should_throw_TENANT_HEADER_MISSING_when_tenant_is_null() {
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(storeId));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.TENANT_HEADER_MISSING);
    }

    @Test
    @DisplayName("should_throw_STORE_NOT_FOUND_when_store_does_not_exist")
    void should_throw_STORE_NOT_FOUND_when_store_does_not_exist() {
        TenantContext.setTenantId(tenantId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(storeId));
        assertThat(ex.getErrorCode()).isEqualTo(CatalogErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("should_return_active_banners_ordered_by_display_order")
    void should_return_active_banners_ordered_by_display_order() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        StoreJpaEntity store = new StoreJpaEntity();
        store.setId(storeId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));

        UUID catId = UUID.randomUUID();
        PromotionBannerJpaEntity banner = PromotionBannerJpaEntity.builder()
                .storeId(storeId)
                .title("Summer Sales: 20% Off")
                .subtitle("Giảm 20% cho toàn bộ thực đơn Sushi")
                .imageUrl("https://storage.ordera.no/summer.jpg")
                .ctaLabel("Learn more")
                .actionType("NAVIGATE_CATEGORY")
                .targetId(catId)
                .active(true)
                .displayOrder(1)
                .build();
        banner.setId(UUID.randomUUID());
        banner.setTenantId(tenantId);

        when(promotionBannerRepository.findActiveBanners(tenantId, storeId)).thenReturn(List.of(banner));

        // Act
        List<PromotionBannerResponse> result = useCase.execute(storeId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Summer Sales: 20% Off");
        assertThat(result.get(0).ctaActionType()).isEqualTo("NAVIGATE_CATEGORY");
        assertThat(result.get(0).targetCategoryId()).isEqualTo(catId);
        assertThat(result.get(0).active()).isTrue();
    }
}
