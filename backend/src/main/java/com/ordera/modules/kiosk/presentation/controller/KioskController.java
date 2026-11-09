package com.ordera.modules.kiosk.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.context.TenantContextHolder;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.kiosk.application.usecase.*;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.presentation.constant.KioskMessages;
import com.ordera.modules.kiosk.presentation.dto.request.KioskCheckoutRequest;
import com.ordera.modules.kiosk.presentation.dto.request.KioskIdentifyRequest;
import com.ordera.modules.kiosk.presentation.dto.request.KioskQuickEarnRequest;
import com.ordera.modules.kiosk.presentation.dto.request.KioskResetSessionRequest;
import com.ordera.modules.kiosk.presentation.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller cho Phân hệ Kiosk POS & Tích Điểm Nhanh Tại Quầy (APIs 7.1 - 7.5).
 * Bắt buộc kiểm tra Header X-Tenant-ID và X-Kiosk-Device-ID.
 */
@RestController
@RequestMapping("/api/v1/kiosk")
@RequiredArgsConstructor
@Tag(name = "Kiosk POS", description = "Các API điều khiển trạm cảm ứng Kiosk tự phục vụ và tích điểm nhanh tại quầy")
public class KioskController {

    private final KioskIdentifyCustomerUseCase kioskIdentifyCustomerUseCase;
    private final KioskQuickEarnUseCase kioskQuickEarnUseCase;
    private final CreateKioskOrderUseCase createKioskOrderUseCase;
    private final ResetKioskSessionUseCase resetKioskSessionUseCase;
    private final GetKioskConfigUseCase getKioskConfigUseCase;

    @PostMapping("/identify")
    @Operation(summary = "7.1: Tra cứu & nhận diện thành viên qua số điện thoại")
    public ResponseEntity<ApiResponse<KioskCustomerInfoResponse>> identifyCustomer(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-Kiosk-Device-ID", required = false) String deviceId,
            @Valid @RequestBody KioskIdentifyRequest request) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        validateDeviceId(deviceId);

        KioskCustomerInfoResponse response = kioskIdentifyCustomerUseCase.execute(tenantId, deviceId, request);
        String message = response.isRegistered() ? KioskMessages.MSG_CUSTOMER_IDENTIFIED : KioskMessages.MSG_CUSTOMER_NOT_REGISTERED;
        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    @PostMapping("/quick-earn")
    @Operation(summary = "7.2: Tích điểm nhanh tại quầy không cần đặt món (POS ngoài)")
    public ResponseEntity<ApiResponse<KioskQuickEarnResponse>> quickEarnPoints(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-Kiosk-Device-ID", required = false) String deviceId,
            @RequestHeader(value = "X-Staff-PIN", required = false) String staffPin,
            @Valid @RequestBody KioskQuickEarnRequest request) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        validateDeviceId(deviceId);

        KioskQuickEarnResponse response = kioskQuickEarnUseCase.execute(tenantId, deviceId, staffPin, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(KioskMessages.MSG_QUICK_POINTS_EARNED, response));
    }

    @PostMapping("/checkout")
    @Operation(summary = "7.3: Tự đặt món & thanh toán trực tiếp tại Kiosk")
    public ResponseEntity<ApiResponse<KioskCheckoutResponse>> checkoutOrder(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-Kiosk-Device-ID", required = false) String deviceId,
            @Valid @RequestBody KioskCheckoutRequest request) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        validateDeviceId(deviceId);

        KioskCheckoutResponse response = createKioskOrderUseCase.execute(tenantId, deviceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(KioskMessages.MSG_KIOSK_ORDER_PLACED, response));
    }

    @PostMapping("/session/reset")
    @Operation(summary = "7.4: Đặt lại phiên làm việc Kiosk (Watchdog 60s hoặc Start Over)")
    public ResponseEntity<ApiResponse<KioskResetSessionResponse>> resetSession(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @RequestHeader(value = "X-Kiosk-Device-ID", required = false) String deviceId,
            @Valid @RequestBody KioskResetSessionRequest request) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        validateDeviceId(deviceId);

        KioskResetSessionResponse response = resetKioskSessionUseCase.execute(tenantId, deviceId, request);
        return ResponseEntity.ok(ApiResponse.ok(KioskMessages.MSG_SESSION_RESET, response));
    }

    @GetMapping("/config/{deviceId}")
    @Operation(summary = "7.5: Cấu hình thiết bị & khởi tạo trạm Kiosk")
    public ResponseEntity<ApiResponse<KioskConfigResponse>> getKioskConfig(
            @RequestHeader(value = "X-Tenant-ID", required = false) UUID tenantIdHeader,
            @PathVariable("deviceId") String deviceId) {

        UUID tenantId = resolveTenantId(tenantIdHeader);
        validateDeviceId(deviceId);

        KioskConfigResponse response = getKioskConfigUseCase.execute(tenantId, deviceId);
        return ResponseEntity.ok(ApiResponse.ok(KioskMessages.MSG_KIOSK_CONFIG_RETRIEVED, response));
    }

    private UUID resolveTenantId(UUID headerTenantId) {
        if (headerTenantId != null) {
            return headerTenantId;
        }
        UUID contextTenantId = TenantContextHolder.getTenantId();
        if (contextTenantId != null) {
            return contextTenantId;
        }
        throw new BusinessException(KioskErrorCode.TENANT_HEADER_MISSING);
    }

    private void validateDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new BusinessException(KioskErrorCode.DEVICE_HEADER_MISSING);
        }
    }
}

