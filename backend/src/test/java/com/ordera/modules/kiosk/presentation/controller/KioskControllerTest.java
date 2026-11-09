package com.ordera.modules.kiosk.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.kiosk.application.usecase.*;
import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.domain.exception.KioskErrorCode;
import com.ordera.modules.kiosk.presentation.constant.KioskMessages;
import com.ordera.modules.kiosk.presentation.dto.request.KioskCheckoutRequest;
import com.ordera.modules.kiosk.presentation.dto.request.KioskIdentifyRequest;
import com.ordera.modules.kiosk.presentation.dto.request.KioskQuickEarnRequest;
import com.ordera.modules.kiosk.presentation.dto.request.KioskResetSessionRequest;
import com.ordera.modules.kiosk.presentation.dto.response.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KioskController Unit Tests")
class KioskControllerTest {

    @Mock
    private KioskIdentifyCustomerUseCase kioskIdentifyCustomerUseCase;

    @Mock
    private KioskQuickEarnUseCase kioskQuickEarnUseCase;

    @Mock
    private CreateKioskOrderUseCase createKioskOrderUseCase;

    @Mock
    private ResetKioskSessionUseCase resetKioskSessionUseCase;

    @Mock
    private GetKioskConfigUseCase getKioskConfigUseCase;

    @InjectMocks
    private KioskController controller;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final String deviceId = "kiosk-pos-sentrum-01";

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should_throw_TENANT_HEADER_MISSING_when_tenant_header_is_missing")
    void should_throw_TENANT_HEADER_MISSING_when_tenant_header_is_missing() {
        KioskIdentifyRequest request = KioskIdentifyRequest.builder()
                .storeId(storeId)
                .phoneNumber("+4791234567")
                .build();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.identifyCustomer(null, deviceId, request));
        assertEquals(KioskErrorCode.TENANT_HEADER_MISSING, ex.getErrorCode());
    }

    @Test
    @DisplayName("should_throw_DEVICE_HEADER_MISSING_when_device_header_is_missing")
    void should_throw_DEVICE_HEADER_MISSING_when_device_header_is_missing() {
        KioskIdentifyRequest request = KioskIdentifyRequest.builder()
                .storeId(storeId)
                .phoneNumber("+4791234567")
                .build();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.identifyCustomer(tenantId, "", request));
        assertEquals(KioskErrorCode.DEVICE_HEADER_MISSING, ex.getErrorCode());
    }

    @Test
    @DisplayName("should_identify_customer_successfully_200_OK")
    void should_identify_customer_successfully_200_OK() {
        KioskIdentifyRequest request = KioskIdentifyRequest.builder()
                .storeId(storeId)
                .phoneNumber("+4791234567")
                .build();

        KioskCustomerInfoResponse responseData = KioskCustomerInfoResponse.builder()
                .isRegistered(true)
                .name("Đào Tâm Đức")
                .phoneNumber("+4791234567")
                .build();

        when(kioskIdentifyCustomerUseCase.execute(tenantId, deviceId, request)).thenReturn(responseData);

        ResponseEntity<ApiResponse<KioskCustomerInfoResponse>> response = controller.identifyCustomer(tenantId, deviceId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(KioskMessages.MSG_CUSTOMER_IDENTIFIED, response.getBody().getMessage());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Đào Tâm Đức", response.getBody().getData().getName());
    }

    @Test
    @DisplayName("should_quick_earn_points_successfully_201_CREATED")
    void should_quick_earn_points_successfully_201_CREATED() {
        KioskQuickEarnRequest request = KioskQuickEarnRequest.builder()
                .storeId(storeId)
                .phoneNumber("+4791234567")
                .billAmount(new BigDecimal("93.00"))
                .build();

        KioskQuickEarnResponse responseData = KioskQuickEarnResponse.builder()
                .pointsEarned(140)
                .queueNumber("#A-08")
                .build();

        when(kioskQuickEarnUseCase.execute(tenantId, deviceId, "1234", request)).thenReturn(responseData);

        ResponseEntity<ApiResponse<KioskQuickEarnResponse>> response = controller.quickEarnPoints(tenantId, deviceId, "1234", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(KioskMessages.MSG_QUICK_POINTS_EARNED, response.getBody().getMessage());
        assertEquals(140, response.getBody().getData().getPointsEarned());
    }

    @Test
    @DisplayName("should_reset_kiosk_session_successfully_200_OK")
    void should_reset_kiosk_session_successfully_200_OK() {
        KioskResetSessionRequest request = KioskResetSessionRequest.builder()
                .storeId(storeId)
                .reason(KioskConstants.REASON_IDLE_TIMEOUT)
                .build();

        KioskResetSessionResponse responseData = KioskResetSessionResponse.builder()
                .kioskStatus(KioskConstants.KIOSK_STATUS_STANDBY)
                .countdownDefaultSeconds(60)
                .build();

        when(resetKioskSessionUseCase.execute(tenantId, deviceId, request)).thenReturn(responseData);

        ResponseEntity<ApiResponse<KioskResetSessionResponse>> response = controller.resetSession(tenantId, deviceId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(KioskMessages.MSG_SESSION_RESET, response.getBody().getMessage());
    }

    @Test
    @DisplayName("should_get_kiosk_config_successfully_200_OK")
    void should_get_kiosk_config_successfully_200_OK() {
        KioskConfigResponse responseData = KioskConfigResponse.builder()
                .deviceId(deviceId)
                .deviceName("Counter Touch Terminal 01")
                .build();

        when(getKioskConfigUseCase.execute(tenantId, deviceId)).thenReturn(responseData);

        ResponseEntity<ApiResponse<KioskConfigResponse>> response = controller.getKioskConfig(tenantId, deviceId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(KioskMessages.MSG_KIOSK_CONFIG_RETRIEVED, response.getBody().getMessage());
        assertEquals("Counter Touch Terminal 01", response.getBody().getData().getDeviceName());
    }
}

