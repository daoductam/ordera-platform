package com.ordera.modules.auth.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.application.port.EmailSenderPort;
import com.ordera.modules.auth.application.port.OtpStoragePort;
import com.ordera.modules.auth.application.port.SmsSenderPort;
import com.ordera.modules.auth.application.usecase.impl.SendRegisterOtpUseCaseImpl;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.infrastructure.persistence.repository.OtpAuditLogRepository;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.auth.presentation.dto.SendRegisterOtpRequest;
import com.ordera.modules.auth.presentation.dto.SendRegisterOtpResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendRegisterOtpUseCase Unit Tests")
class SendRegisterOtpUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpStoragePort otpStoragePort;

    @Mock
    private SmsSenderPort smsSenderPort;

    @Mock
    private EmailSenderPort emailSenderPort;

    @Mock
    private OtpAuditLogRepository otpAuditLogRepository;

    private SendRegisterOtpUseCaseImpl sendRegisterOtpUseCase;
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
        sendRegisterOtpUseCase = new SendRegisterOtpUseCaseImpl(
                userRepository, otpStoragePort, smsSenderPort, emailSenderPort, otpAuditLogRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should throw USER_ALREADY_EXISTS when phone or email already registered")
    void should_throwException_when_userAlreadyExists() {
        SendRegisterOtpRequest request = SendRegisterOtpRequest.builder()
                .channel("EMAIL")
                .phoneNumber("+4797362748")
                .email("abytran@ordera.no")
                .fullName("ABY TRAN")
                .build();

        when(userRepository.existsByTenantIdAndPhoneNumber(tenantId, request.getPhoneNumber())).thenReturn(true);

        assertThatThrownBy(() -> sendRegisterOtpUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(AuthErrorCode.USER_ALREADY_EXISTS));

        verify(emailSenderPort, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("should send OTP via Email when channel is EMAIL")
    void should_sendOtpViaEmail() {
        SendRegisterOtpRequest request = SendRegisterOtpRequest.builder()
                .channel("EMAIL")
                .phoneNumber("+4797362748")
                .email("abytran@ordera.no")
                .fullName("ABY TRAN")
                .build();

        when(userRepository.existsByTenantIdAndPhoneNumber(tenantId, request.getPhoneNumber())).thenReturn(false);
        when(userRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())).thenReturn(false);
        when(otpStoragePort.isCooldownActive(tenantId, request.getEmail())).thenReturn(false);

        SendRegisterOtpResponse response = sendRegisterOtpUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getChannel()).isEqualTo("EMAIL");
        assertThat(response.getRecipientMasked()).isEqualTo("a***n@ordera.no");

        verify(otpStoragePort).saveOtp(eq(tenantId), eq(request.getEmail()), anyString(), eq(120L));
        verify(emailSenderPort).sendEmail(eq(request.getEmail()), anyString(), anyString());
    }
}
