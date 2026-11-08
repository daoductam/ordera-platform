package com.ordera.modules.auth.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.application.port.OtpStoragePort;
import com.ordera.modules.auth.application.port.SmsSenderPort;
import com.ordera.modules.auth.application.usecase.impl.SendOtpUseCaseImpl;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.infrastructure.persistence.entity.OtpAuditLogJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.OtpAuditLogRepository;
import com.ordera.modules.auth.presentation.dto.SendOtpRequest;
import com.ordera.modules.auth.presentation.dto.SendOtpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendOtpUseCase Unit Tests")
class SendOtpUseCaseTest {

    @Mock
    private OtpStoragePort otpStoragePort;

    @Mock
    private SmsSenderPort smsSenderPort;

    @Mock
    private OtpAuditLogRepository otpAuditLogRepository;

    private SendOtpUseCaseImpl sendOtpUseCase;
    private final UUID tenantId = UUID.randomUUID();
    private final String phoneNumber = "+4797362748";

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
        sendOtpUseCase = new SendOtpUseCaseImpl(otpStoragePort, smsSenderPort, otpAuditLogRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should throw TENANT_HEADER_MISSING when tenant context is null")
    void should_throwException_when_tenantMissing() {
        TenantContext.clear();
        SendOtpRequest request = SendOtpRequest.builder().phoneNumber(phoneNumber).build();

        assertThatThrownBy(() -> sendOtpUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(AuthErrorCode.TENANT_HEADER_MISSING));
    }

    @Test
    @DisplayName("should throw OTP_COOLDOWN_ACTIVE when cooldown is active")
    void should_throwException_when_cooldownActive() {
        when(otpStoragePort.isCooldownActive(tenantId, phoneNumber)).thenReturn(true);
        when(otpStoragePort.getCooldownRemainingSeconds(tenantId, phoneNumber)).thenReturn(74L);

        SendOtpRequest request = SendOtpRequest.builder().phoneNumber(phoneNumber).build();

        assertThatThrownBy(() -> sendOtpUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(AuthErrorCode.OTP_COOLDOWN_ACTIVE));

        verify(smsSenderPort, never()).sendSms(anyString(), anyString());
    }

    @Test
    @DisplayName("should successfully send OTP SMS and record audit log when cooldown is inactive")
    void should_sendOtpSuccessfully_when_cooldownInactive() {
        when(otpStoragePort.isCooldownActive(tenantId, phoneNumber)).thenReturn(false);

        SendOtpRequest request = SendOtpRequest.builder().phoneNumber(phoneNumber).build();
        SendOtpResponse response = sendOtpUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(response.getMaskedPhoneNumber()).isEqualTo("** ***748");
        assertThat(response.getCooldownSeconds()).isEqualTo(120);

        verify(otpStoragePort).saveOtp(eq(tenantId), eq(phoneNumber), anyString(), eq(120L));
        verify(smsSenderPort).sendSms(eq(phoneNumber), contains("Mã xác thực Ordera"));
        verify(otpAuditLogRepository).save(any(OtpAuditLogJpaEntity.class));
    }
}
