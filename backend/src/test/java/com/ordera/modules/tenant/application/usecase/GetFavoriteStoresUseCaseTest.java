package com.ordera.modules.tenant.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.tenant.application.usecase.impl.GetFavoriteStoresUseCaseImpl;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.infrastructure.mapper.TenantDtoMapper;
import com.ordera.modules.tenant.infrastructure.persistence.entity.CustomerFavoriteStoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.CustomerFavoriteStoreRepository;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.presentation.dto.StoreSummaryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFavoriteStoresUseCase Unit Tests")
class GetFavoriteStoresUseCaseTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CustomerFavoriteStoreRepository customerFavoriteStoreRepository;

    @Mock
    private TenantDtoMapper tenantDtoMapper;

    @InjectMocks
    private GetFavoriteStoresUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
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
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(userId, null, null));
        assertEquals(TenantErrorCode.TENANT_HEADER_MISSING, ex.getErrorCode());
    }

    @Test
    @DisplayName("should_return_empty_list_when_user_has_no_favorite_stores")
    void should_return_empty_list_when_user_has_no_favorite_stores() {
        TenantContext.setTenantId(tenantId);
        when(customerFavoriteStoreRepository.findByTenantIdAndUserId(tenantId, userId))
                .thenReturn(Collections.emptyList());

        List<StoreSummaryResponse> actual = useCase.execute(userId, null, null);

        assertTrue(actual.isEmpty());
        verifyNoInteractions(storeRepository, tenantDtoMapper);
    }

    @Test
    @DisplayName("should_return_favorite_stores_with_distance_when_favorites_exist")
    void should_return_favorite_stores_with_distance_when_favorites_exist() {
        // Arrange
        TenantContext.setTenantId(tenantId);

        CustomerFavoriteStoreJpaEntity fav = CustomerFavoriteStoreJpaEntity.builder()
                .userId(userId)
                .storeId(storeId)
                .build();
        fav.setTenantId(tenantId);

        StoreJpaEntity store = StoreJpaEntity.builder()
                .name("Linn Sushi Sentrum")
                .latitude(new BigDecimal("59.9114910"))
                .longitude(new BigDecimal("10.7482320"))
                .active(true)
                .build();
        store.setId(storeId);
        store.setTenantId(tenantId);

        StoreSummaryResponse summary = StoreSummaryResponse.builder()
                .id(storeId)
                .name("Linn Sushi Sentrum")
                .isFavorite(true)
                .build();

        when(customerFavoriteStoreRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(List.of(fav));
        when(storeRepository.findAllById(List.of(storeId))).thenReturn(List.of(store));
        when(tenantDtoMapper.toStoreSummaryResponse(eq(store), anyDouble(), eq(true))).thenReturn(summary);

        // Act
        List<StoreSummaryResponse> actual = useCase.execute(userId, 59.9110, 10.7480);

        // Assert
        assertEquals(1, actual.size());
        assertEquals(storeId, actual.get(0).getId());
        assertTrue(actual.get(0).isFavorite());
    }
}
