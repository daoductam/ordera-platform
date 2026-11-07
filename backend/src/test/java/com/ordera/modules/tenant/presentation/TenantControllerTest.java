package com.ordera.modules.tenant.presentation;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.tenant.application.usecase.GetTenantInfoUseCase;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.presentation.dto.TenantResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantController WebMvc Tests")
class TenantControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetTenantInfoUseCase getTenantInfoUseCase;

    @InjectMocks
    private TenantController tenantController;

    private final UUID tenantId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tenantController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/tenants/current - should return 200 with Tenant details")
    void should_return_200_when_tenant_exists() throws Exception {
        TenantResponse response = TenantResponse.builder()
                .id(tenantId)
                .code("linn_sushi")
                .name("Linn Sushi")
                .slogan("Where fresh sushi meet your taste")
                .primaryColor("#F5B800")
                .secondaryColor("#1E293B")
                .status("ACTIVE")
                .currency("NOK")
                .defaultVatRate(new BigDecimal("0.1500"))
                .build();

        when(getTenantInfoUseCase.execute()).thenReturn(response);

        mockMvc.perform(get("/api/v1/tenants/current")
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tenant details retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(tenantId.toString()))
                .andExpect(jsonPath("$.data.code").value("linn_sushi"))
                .andExpect(jsonPath("$.data.name").value("Linn Sushi"))
                .andExpect(jsonPath("$.data.primaryColor").value("#F5B800"));

        verify(getTenantInfoUseCase).execute();
    }

    @Test
    @DisplayName("GET /api/v1/tenants/current - should return 400 when X-Tenant-ID header is missing")
    void should_return_400_when_tenant_header_is_missing() throws Exception {
        when(getTenantInfoUseCase.execute()).thenThrow(new BusinessException(TenantErrorCode.TENANT_HEADER_MISSING));

        mockMvc.perform(get("/api/v1/tenants/current")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TENANT_HEADER_MISSING"));
    }

    @Test
    @DisplayName("GET /api/v1/tenants/current - should return 404 when tenant not found")
    void should_return_404_when_tenant_not_found() throws Exception {
        when(getTenantInfoUseCase.execute()).thenThrow(new BusinessException(TenantErrorCode.TENANT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/tenants/current")
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TENANT_NOT_FOUND"));
    }
}
