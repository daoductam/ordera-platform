package com.ordera.modules.tenant.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.tenant.application.usecase.impl.GetStoreTablesUseCaseImpl;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.infrastructure.mapper.TenantDtoMapper;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreTableJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreTableRepository;
import com.ordera.modules.tenant.presentation.dto.StoreTableResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStoreTablesUseCase Unit Tests")
class GetStoreTablesUseCaseTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreTableRepository storeTableRepository;

    @Mock
    private TenantDtoMapper tenantDtoMapper;

    @InjectMocks
    private GetStoreTablesUseCaseImpl useCase;

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
        assertEquals(TenantErrorCode.TENANT_HEADER_MISSING, ex.getErrorCode());
    }

    @Test
    @DisplayName("should_throw_STORE_NOT_FOUND_when_store_does_not_exist")
    void should_throw_STORE_NOT_FOUND_when_store_does_not_exist() {
        TenantContext.setTenantId(tenantId);
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute(storeId));
        assertEquals(TenantErrorCode.STORE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("should_return_list_of_tables_when_store_exists")
    void should_return_list_of_tables_when_store_exists() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        StoreJpaEntity store = StoreJpaEntity.builder().name("Sentrum").build();
        store.setId(storeId);

        UUID tableId = UUID.randomUUID();
        StoreTableJpaEntity table = StoreTableJpaEntity.builder()
                .storeId(storeId)
                .tableNumber("Table 01")
                .zone("Indoor")
                .seatingCapacity(4)
                .build();
        table.setId(tableId);

        StoreTableResponse response = StoreTableResponse.builder()
                .id(tableId)
                .tableNumber("Table 01")
                .build();

        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));
        when(storeTableRepository.findByTenantIdAndStoreId(tenantId, storeId)).thenReturn(List.of(table));
        when(tenantDtoMapper.toStoreTableResponse(table)).thenReturn(response);

        // Act
        List<StoreTableResponse> actual = useCase.execute(storeId);

        // Assert
        assertEquals(1, actual.size());
        assertEquals("Table 01", actual.get(0).getTableNumber());
    }
}
