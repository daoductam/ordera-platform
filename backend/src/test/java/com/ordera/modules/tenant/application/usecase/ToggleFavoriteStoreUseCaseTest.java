package com.ordera.modules.tenant.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.tenant.application.usecase.impl.ToggleFavoriteStoreUseCaseImpl;
import com.ordera.modules.tenant.domain.exception.StoreNotFoundException;
import com.ordera.modules.tenant.domain.exception.TenantHeaderMissingException;
import com.ordera.modules.tenant.infrastructure.persistence.entity.CustomerFavoriteStoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.CustomerFavoriteStoreRepository;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.presentation.dto.ToggleFavoriteResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToggleFavoriteStoreUseCase Unit Tests")
class ToggleFavoriteStoreUseCaseTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private CustomerFavoriteStoreRepository customerFavoriteStoreRepository;

    @InjectMocks
    private ToggleFavoriteStoreUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
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
        assertThrows(TenantHeaderMissingException.class, () -> useCase.execute(storeId, userId, true));
    }

    @Test
    @DisplayName("should_throw_StoreNotFoundException_when_store_does_not_exist")
    void should_throw_StoreNotFoundException_when_store_does_not_exist() {
        TenantContext.setTenantId(tenantId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.empty());

        assertThrows(StoreNotFoundException.class, () -> useCase.execute(storeId, userId, true));
    }

    @Test
    @DisplayName("should_add_to_favorites_when_favorite_is_true_and_not_yet_favorited")
    void should_add_to_favorites_when_favorite_is_true_and_not_yet_favorited() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        StoreJpaEntity store = StoreJpaEntity.builder().name("Sentrum").build();
        store.setId(storeId);

        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));
        when(customerFavoriteStoreRepository.existsByTenantIdAndUserIdAndStoreId(tenantId, userId, storeId)).thenReturn(false);

        // Act
        ToggleFavoriteResponse response = useCase.execute(storeId, userId, true);

        // Assert
        assertNotNull(response);
        assertEquals(storeId, response.getStoreId());
        assertTrue(response.isFavorite());
        verify(customerFavoriteStoreRepository).save(any(CustomerFavoriteStoreJpaEntity.class));
    }

    @Test
    @DisplayName("should_remove_from_favorites_when_favorite_is_false")
    void should_remove_from_favorites_when_favorite_is_false() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        StoreJpaEntity store = StoreJpaEntity.builder().name("Sentrum").build();
        store.setId(storeId);

        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));

        // Act
        ToggleFavoriteResponse response = useCase.execute(storeId, userId, false);

        // Assert
        assertNotNull(response);
        assertEquals(storeId, response.getStoreId());
        assertFalse(response.isFavorite());
        verify(customerFavoriteStoreRepository).deleteByTenantIdAndUserIdAndStoreId(tenantId, userId, storeId);
    }
}

