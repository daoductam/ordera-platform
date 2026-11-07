package com.ordera.modules.tenant.presentation;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.tenant.application.usecase.GetTenantInfoUseCase;
import com.ordera.modules.tenant.presentation.dto.TenantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "01. Tenant", description = "Cấu hình thương hiệu & đại lý F&B")
public class TenantController {

    private final GetTenantInfoUseCase getTenantInfoUseCase;

    @Operation(summary = "Lấy cấu hình thương hiệu hiện tại", description = "Lấy thông tin cấu hình thương hiệu (logo, slogan, màu sắc, hotline) phục vụ Dynamic Theming")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<TenantResponse>> getCurrentTenant() {
        TenantResponse response = getTenantInfoUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok("Tenant details retrieved successfully", response));
    }
}

