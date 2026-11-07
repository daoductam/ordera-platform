package com.ordera.modules.tenant.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.tenant.application.usecase.impl.GetTenantInfoUseCaseImpl;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.infrastructure.mapper.TenantDtoMapper;
import com.ordera.modules.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.TenantRepository;
import com.ordera.modules.tenant.presentation.dto.TenantResponse;
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
@DisplayName("GetTenantInfoUseCase Unit Tests")
class GetTenantInfoUseCaseTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantDtoMapper tenantDtoMapper;

    @InjectMocks
    private GetTenantInfoUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should_throw_TENANT_HEADER_MISSING_when_tenant_context_is_empty")
    void should_throw_TENANT_HEADER_MISSING_when_tenant_context_is_empty() {
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute());
        assertEquals(TenantErrorCode.TENANT_HEADER_MISSING, ex.getErrorCode());
        verifyNoInteractions(tenantRepository, tenantDtoMapper);
    }

    @Test
    @DisplayName("should_throw_TENANT_NOT_FOUND_when_tenant_does_not_exist")
    void should_throw_TENANT_NOT_FOUND_when_tenant_does_not_exist() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> useCase.execute());
        assertEquals(TenantErrorCode.TENANT_NOT_FOUND, ex.getErrorCode());
        verify(tenantRepository).findById(tenantId);
        verifyNoInteractions(tenantDtoMapper);
    }

    @Test
    @DisplayName("should_return_TenantResponse_when_tenant_exists")
    void should_return_TenantResponse_when_tenant_exists() {
        // Arrange
        TenantContext.setTenantId(tenantId);
        TenantJpaEntity entity = TenantJpaEntity.builder()
                .code("linn_sushi")
                .name("Linn Sushi")
                .build();
        entity.setId(tenantId);

        TenantResponse expectedResponse = TenantResponse.builder()
                .id(tenantId)
                .code("linn_sushi")
                .name("Linn Sushi")
                .build();

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(entity));
        when(tenantDtoMapper.toTenantResponse(entity)).thenReturn(expectedResponse);

        // Act
        TenantResponse actual = useCase.execute();

        // Assert
        assertNotNull(actual);
        assertEquals(tenantId, actual.getId());
        assertEquals("linn_sushi", actual.getCode());
        verify(tenantRepository).findById(tenantId);
        verify(tenantDtoMapper).toTenantResponse(entity);
    }
}
