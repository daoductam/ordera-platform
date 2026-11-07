package com.ordera.modules.tenant.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.tenant.application.usecase.impl.GetStoreDetailUseCaseImpl;
import com.ordera.modules.tenant.domain.exception.StoreNotFoundException;
import com.ordera.modules.tenant.domain.exception.TenantHeaderMissingException;
import com.ordera.modules.tenant.infrastructure.mapper.TenantDtoMapper;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.presentation.dto.StoreDetailResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStoreDetailUseCase Unit Tests")
class GetStoreDetailUseCaseTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private TenantDtoMapper tenantDtoMapper;

    @InjectMocks
    private GetStoreDetailUseCaseImpl useCase;

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
    @DisplayName("should_throw_TenantHeaderMissingException_when_tenant_is_null")
    void should_throw_TenantHeaderMissingException_when_tenant_is_null() {
        assertThrows(TenantHeaderMissingException.class, () -> useCase.execute(storeId));
        verifyNoInteractions(storeRepository, tenantDtoMapper);
    }

    @Test
    @DisplayName("should_throw_StoreNotFoundException_when_store_does_not_exist_in_tenant")
    void should_throw_StoreNotFoundException_when_store_does_not_exist_in_tenant() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(StoreNotFoundException.class, () -> useCase.execute(storeId));
        verify(storeRepository).findByIdAndTenantId(storeId, tenantId);
        verifyNoInteractions(tenantDtoMapper);
    }

    @Test
    @DisplayName("should_return_StoreDetailResponse_when_store_exists")
    void should_return_StoreDetailResponse_when_store_exists() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        StoreJpaEntity store = StoreJpaEntity.builder().name("Linn Sushi Sentrum").build();
        store.setId(storeId);

        StoreDetailResponse response = StoreDetailResponse.builder()
                .id(storeId)
                .name("Linn Sushi Sentrum")
                .isOpen(true)
                .build();

        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));
        when(tenantDtoMapper.toStoreDetailResponse(store)).thenReturn(response);

        // Act
        StoreDetailResponse actual = useCase.execute(storeId);

        // Assert
        assertNotNull(actual);
        assertEquals(storeId, actual.getId());
        assertEquals("Linn Sushi Sentrum", actual.getName());
        verify(storeRepository).findByIdAndTenantId(storeId, tenantId);
        verify(tenantDtoMapper).toStoreDetailResponse(store);
    }
}

