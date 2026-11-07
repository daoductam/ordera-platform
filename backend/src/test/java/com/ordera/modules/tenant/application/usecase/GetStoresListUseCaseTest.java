package com.ordera.modules.tenant.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.tenant.application.usecase.impl.GetStoresListUseCaseImpl;
import com.ordera.modules.tenant.domain.exception.TenantHeaderMissingException;
import com.ordera.modules.tenant.domain.model.FulfillmentType;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStoresListUseCase Unit Tests")
class GetStoresListUseCaseTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CustomerFavoriteStoreRepository customerFavoriteStoreRepository;

    @Mock
    private TenantDtoMapper tenantDtoMapper;

    @InjectMocks
    private GetStoresListUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should_throw_TenantHeaderMissingException_when_tenant_is_null")
    void should_throw_TenantHeaderMissingException_when_tenant_is_null() {
        // Arrange & Act & Assert
        assertThrows(TenantHeaderMissingException.class,
                () -> useCase.execute(null, FulfillmentType.ALL, null, null, null));
    }

    @Test
    @DisplayName("should_calculate_accurate_haversine_distance_between_oslo_coordinates")
    void should_calculate_accurate_haversine_distance_between_oslo_coordinates() {
        // Oslo Central Station (59.9111, 10.7528) to Majorstuen (59.9272, 10.7163) ~ 2.7 km
        Double distance = GetStoresListUseCaseImpl.calculateHaversineDistance(
                59.9111, 10.7528,
                59.9272, 10.7163
        );

        assertNotNull(distance);
        assertTrue(distance > 2.5 && distance < 3.0, "Distance should be around 2.7 km, actual: " + distance);
    }

    @Test
    @DisplayName("should_filter_by_fulfillment_type_and_sort_by_distance_ascending")
    void should_filter_by_fulfillment_type_and_sort_by_distance_ascending() {
        // Arrange
        TenantContext.setTenantId(tenantId);

        UUID storeFarId = UUID.randomUUID();
        StoreJpaEntity storeFar = StoreJpaEntity.builder()
                .name("Store Far")
                .address("Bogstadveien 45")
                .city("Oslo")
                .latitude(new BigDecimal("59.9272300"))
                .longitude(new BigDecimal("10.7162900"))
                .acceptsDelivery(true)
                .build();
        storeFar.setId(storeFarId);

        UUID storeNearId = UUID.randomUUID();
        StoreJpaEntity storeNear = StoreJpaEntity.builder()
                .name("Store Near")
                .address("Prinsens gate 10C")
                .city("Oslo")
                .latitude(new BigDecimal("59.9114910"))
                .longitude(new BigDecimal("10.7482320"))
                .acceptsDelivery(true)
                .build();
        storeNear.setId(storeNearId);

        when(storeRepository.searchStores(eq(tenantId), isNull()))
                .thenReturn(List.of(storeFar, storeNear));

        when(tenantDtoMapper.toStoreSummaryResponse(eq(storeFar), anyDouble(), anyBoolean()))
                .thenAnswer(inv -> StoreSummaryResponse.builder()
                        .id(storeFarId)
                        .name("Store Far")
                        .distanceKm(inv.getArgument(1))
                        .build());

        when(tenantDtoMapper.toStoreSummaryResponse(eq(storeNear), anyDouble(), anyBoolean()))
                .thenAnswer(inv -> StoreSummaryResponse.builder()
                        .id(storeNearId)
                        .name("Store Near")
                        .distanceKm(inv.getArgument(1))
                        .build());

        // Act: User near Oslo Sentrum (59.9110, 10.7480)
        List<StoreSummaryResponse> result = useCase.execute(
                null,
                FulfillmentType.DELIVERY,
                59.9110,
                10.7480,
                null
        );

        // Assert: Store Near must be first
        assertEquals(2, result.size());
        assertEquals("Store Near", result.get(0).getName());
        assertEquals("Store Far", result.get(1).getName());
        assertTrue(result.get(0).getDistanceKm() < result.get(1).getDistanceKm());
    }

    @Test
    @DisplayName("should_mark_isFavorite_true_when_user_has_favorited_the_store")
    void should_mark_isFavorite_true_when_user_has_favorited_the_store() {
        // Arrange
        TenantContext.setTenantId(tenantId);

        UUID storeId = UUID.randomUUID();
        StoreJpaEntity store = StoreJpaEntity.builder()
                .name("Linn Sushi Sentrum")
                .build();
        store.setId(storeId);

        CustomerFavoriteStoreJpaEntity fav = CustomerFavoriteStoreJpaEntity.builder()
                .userId(userId)
                .storeId(storeId)
                .build();

        when(storeRepository.searchStores(eq(tenantId), isNull())).thenReturn(List.of(store));
        when(customerFavoriteStoreRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(List.of(fav));
        when(tenantDtoMapper.toStoreSummaryResponse(eq(store), isNull(), eq(true)))
                .thenReturn(StoreSummaryResponse.builder().id(storeId).isFavorite(true).build());

        // Act
        List<StoreSummaryResponse> result = useCase.execute(null, FulfillmentType.ALL, null, null, userId);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).isFavorite());
    }
}

