package com.ordera.modules.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.auth.application.usecase.*;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.presentation.dto.*;
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

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController WebMvc Tests")
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SendOtpUseCase sendOtpUseCase;

    @Mock
    private VerifyOtpUseCase verifyOtpUseCase;

    @Mock
    private SendRegisterOtpUseCase sendRegisterOtpUseCase;

    @Mock
    private VerifyRegisterOtpUseCase verifyRegisterOtpUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @Mock
    private LogoutUseCase logoutUseCase;

    @InjectMocks
    private AuthController authController;

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String TENANT_ID = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/send - should return 200 with masked phone number")
    void should_sendOtp_return200() throws Exception {
        SendOtpRequest request = SendOtpRequest.builder()
                .channel("SMS")
                .phoneNumber("+4797362748")
                .build();

        SendOtpResponse response = SendOtpResponse.builder()
                .phoneNumber("+4797362748")
                .maskedPhoneNumber("** ***748")
                .channel("SMS")
                .cooldownSeconds(120)
                .resendAllowedAfterSeconds(120)
                .build();

        when(sendOtpUseCase.execute(any(SendOtpRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.phoneNumber").value("+4797362748"))
                .andExpect(jsonPath("$.data.maskedPhoneNumber").value("** ***748"))
                .andExpect(jsonPath("$.data.cooldownSeconds").value(120));
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/send - should return 429 when cooldown is active")
    void should_sendOtp_return429_when_cooldownActive() throws Exception {
        SendOtpRequest request = SendOtpRequest.builder()
                .channel("SMS")
                .phoneNumber("+4797362748")
                .build();

        when(sendOtpUseCase.execute(any(SendOtpRequest.class)))
                .thenThrow(new BusinessException(AuthErrorCode.OTP_COOLDOWN_ACTIVE, "Vui lòng đợi thêm 74 giây", Map.of("retryAfterSeconds", 74)));

        mockMvc.perform(post("/api/v1/auth/otp/send")
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("OTP_COOLDOWN_ACTIVE"))
                .andExpect(jsonPath("$.error.details.retryAfterSeconds").value(74));
    }

    @Test
    @DisplayName("POST /api/v1/auth/otp/verify - should return 200 with JWT tokens when login success")
    void should_verifyOtp_return200_when_loginSuccess() throws Exception {
        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .phoneNumber("+4797362748")
                .otpCode("849201")
                .build();

        AuthUserResponse user = AuthUserResponse.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+4797362748")
                .email("david.nguyen@ordera.no")
                .fullName("David Nguyen")
                .role("CUSTOMER")
                .tierName("Silver")
                .currentPoints(120)
                .build();

        VerifyOtpResponse response = VerifyOtpResponse.builder()
                .isProfileComplete(true)
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(user)
                .build();

        when(verifyOtpUseCase.execute(any(VerifyOtpRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/otp/verify")
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isProfileComplete").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.user.fullName").value("David Nguyen"))
                .andExpect(jsonPath("$.data.user.tierName").value("Silver"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register/otp/verify - should return 201 Created when register success")
    void should_verifyRegisterOtp_return201() throws Exception {
        VerifyRegisterOtpRequest request = VerifyRegisterOtpRequest.builder()
                .phoneNumber("+4797362748")
                .email("abytran@ordera.no")
                .fullName("ABY TRAN")
                .dateOfBirth(LocalDate.of(2000, 8, 23))
                .otpCode("387505")
                .build();

        AuthUserResponse user = AuthUserResponse.builder()
                .id(UUID.randomUUID())
                .phoneNumber("+4797362748")
                .email("abytran@ordera.no")
                .fullName("ABY TRAN")
                .build();

        LoyaltyTierDetailDto loyalty = LoyaltyTierDetailDto.builder()
                .tierName("New")
                .tierOrder(1)
                .themeColorHex("#1EB759")
                .currentPoints(0)
                .build();

        VerifyRegisterOtpResponse response = VerifyRegisterOtpResponse.builder()
                .accessToken("reg-access-token")
                .refreshToken("reg-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(user)
                .loyalty(loyalty)
                .build();

        when(verifyRegisterOtpUseCase.execute(any(VerifyRegisterOtpRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register/otp/verify")
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("reg-access-token"))
                .andExpect(jsonPath("$.data.loyalty.tierName").value("New"))
                .andExpect(jsonPath("$.data.loyalty.currentPoints").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh-token - should return 200 with new tokens")
    void should_refreshToken_return200() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("old-refresh-token")
                .build();

        RefreshTokenResponse response = RefreshTokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();

        when(refreshTokenUseCase.execute(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .header(TENANT_HEADER, TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - should return 200 OK")
    void should_logout_return200() throws Exception {
        LogoutRequest request = LogoutRequest.builder().refreshToken("refresh-token").build();

        doNothing().when(logoutUseCase).execute(anyString(), any(LogoutRequest.class));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(TENANT_HEADER, TENANT_ID)
                        .header("Authorization", "Bearer sample-access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công. Phiên làm việc đã bị hủy."));
    }
}

