package com.ordera.modules.tenant.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.tenant.application.usecase.impl.ResolveTableQrUseCaseImpl;
import com.ordera.modules.tenant.domain.exception.InvalidQrTokenException;
import com.ordera.modules.tenant.domain.exception.TenantHeaderMissingException;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreTableJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreTableRepository;
import com.ordera.modules.tenant.presentation.dto.ResolvedTableQrResponse;
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
@DisplayName("ResolveTableQrUseCase Unit Tests")
class ResolveTableQrUseCaseTest {

    @Mock
    private StoreTableRepository storeTableRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ResolveTableQrUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final UUID tableId = UUID.randomUUID();

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
        assertThrows(TenantHeaderMissingException.class, () -> useCase.execute("qr_linn_sentrum_table_05"));
    }

    @Test
    @DisplayName("should_throw_InvalidQrTokenException_when_token_is_blank")
    void should_throw_InvalidQrTokenException_when_token_is_blank() {
        TenantContext.setTenantId(tenantId);
        assertThrows(InvalidQrTokenException.class, () -> useCase.execute("   "));
    }

    @Test
    @DisplayName("should_throw_InvalidQrTokenException_when_qr_token_does_not_exist")
    void should_throw_InvalidQrTokenException_when_qr_token_does_not_exist() {
        TenantContext.setTenantId(tenantId);
        when(storeTableRepository.findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, "non_existent_token"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidQrTokenException.class, () -> useCase.execute("non_existent_token"));
    }

    @Test
    @DisplayName("should_resolve_table_when_qr_token_matches_exact_in_db")
    void should_resolve_table_when_qr_token_matches_exact_in_db() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        String token = "qr_linn_sentrum_table_05";

        StoreTableJpaEntity table = StoreTableJpaEntity.builder()
                .storeId(storeId)
                .tableNumber("Table 05")
                .zone("Tầng 1 - Khu trong nhà")
                .seatingCapacity(4)
                .occupied(false)
                .qrCodeToken(token)
                .build();
        table.setId(tableId);
        table.setTenantId(tenantId);

        StoreJpaEntity store = StoreJpaEntity.builder()
                .name("Linn Sushi Sentrum")
                .build();
        store.setId(storeId);
        store.setTenantId(tenantId);

        when(storeTableRepository.findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, token))
                .thenReturn(Optional.of(table));
        when(storeRepository.findByIdAndTenantId(storeId, tenantId))
                .thenReturn(Optional.of(store));

        // Act
        ResolvedTableQrResponse actual = useCase.execute(token);

        // Assert
        assertNotNull(actual);
        assertEquals(storeId, actual.getStoreId());
        assertEquals("Linn Sushi Sentrum", actual.getStoreName());
        assertEquals(tableId, actual.getTableId());
        assertEquals("Table 05", actual.getTableNumber());
        assertEquals("Tầng 1 - Khu trong nhà", actual.getZone());
        assertEquals(4, actual.getCapacity());
        assertFalse(actual.isOccupied());
    }

    @Test
    @DisplayName("should_resolve_table_when_token_is_QR_LINN_SENTRUM_T05_via_table_number_fallback")
    void should_resolve_table_when_token_is_QR_LINN_SENTRUM_T05_via_table_number_fallback() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        String token = "QR-LINN-SENTRUM-T05";

        StoreTableJpaEntity table = StoreTableJpaEntity.builder()
                .storeId(storeId)
                .tableNumber("Table 05")
                .zone("Tầng 1")
                .seatingCapacity(4)
                .occupied(false)
                .qrCodeToken("qr_linn_tbl_05")
                .build();
        table.setId(tableId);
        table.setTenantId(tenantId);

        StoreJpaEntity store = StoreJpaEntity.builder()
                .name("Linn Sushi Sentrum")
                .build();
        store.setId(storeId);
        store.setTenantId(tenantId);

        when(storeTableRepository.findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, token))
                .thenReturn(Optional.empty());
        when(storeTableRepository.findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, "QR_LINN_SENTRUM_T05"))
                .thenReturn(Optional.empty());
        when(storeTableRepository.findAll())
                .thenReturn(List.of(table));
        when(storeRepository.findByIdAndTenantId(storeId, tenantId))
                .thenReturn(Optional.of(store));

        // Act
        ResolvedTableQrResponse actual = useCase.execute(token);

        // Assert
        assertNotNull(actual);
        assertEquals("Table 05", actual.getTableNumber());
        assertEquals("Linn Sushi Sentrum", actual.getStoreName());
    }
}

