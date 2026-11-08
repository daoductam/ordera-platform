package com.ordera.modules.auth.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.application.port.OtpStoragePort;
import com.ordera.modules.auth.application.service.JwtTokenProviderService;
import com.ordera.modules.auth.application.usecase.impl.VerifyOtpUseCaseImpl;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.TokenPair;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.OtpAuditLogRepository;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.auth.presentation.dto.VerifyOtpRequest;
import com.ordera.modules.auth.presentation.dto.VerifyOtpResponse;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyOtpUseCase Unit Tests")
class VerifyOtpUseCaseTest {

    @Mock
    private OtpStoragePort otpStoragePort;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpAuditLogRepository otpAuditLogRepository;

    @Mock
    private MemberProfileRepository memberProfileRepository;

    @Mock
    private TierDefinitionRepository tierDefinitionRepository;

    @Mock
    private JwtTokenProviderService jwtTokenProviderService;

    private VerifyOtpUseCaseImpl verifyOtpUseCase;
    private final UUID tenantId = UUID.randomUUID();
    private final String phoneNumber = "+4797362748";

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
        verifyOtpUseCase = new VerifyOtpUseCaseImpl(
                otpStoragePort, userRepository, otpAuditLogRepository,
                memberProfileRepository, tierDefinitionRepository, jwtTokenProviderService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should throw OTP_MAX_ATTEMPTS_EXCEEDED when attempts >= 5")
    void should_throwException_when_maxAttemptsExceeded() {
        when(otpStoragePort.getAttempts(tenantId, phoneNumber)).thenReturn(5);

        VerifyOtpRequest request = VerifyOtpRequest.builder().phoneNumber(phoneNumber).otpCode("123456").build();

        assertThatThrownBy(() -> verifyOtpUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED));
    }

    @Test
    @DisplayName("should throw OTP_EXPIRED when OTP not found in cache")
    void should_throwException_when_otpExpired() {
        when(otpStoragePort.getAttempts(tenantId, phoneNumber)).thenReturn(0);
        when(otpStoragePort.getOtp(tenantId, phoneNumber)).thenReturn(Optional.empty());

        VerifyOtpRequest request = VerifyOtpRequest.builder().phoneNumber(phoneNumber).otpCode("123456").build();

        assertThatThrownBy(() -> verifyOtpUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(AuthErrorCode.OTP_EXPIRED));
    }

    @Test
    @DisplayName("should throw OTP_INVALID and increment attempts when OTP does not match")
    void should_throwException_when_otpInvalid() {
        when(otpStoragePort.getAttempts(tenantId, phoneNumber)).thenReturn(0);
        when(otpStoragePort.getOtp(tenantId, phoneNumber)).thenReturn(Optional.of("654321"));
        when(otpStoragePort.incrementAttempts(eq(tenantId), eq(phoneNumber), anyLong())).thenReturn(1);

        VerifyOtpRequest request = VerifyOtpRequest.builder().phoneNumber(phoneNumber).otpCode("123456").build();

        assertThatThrownBy(() -> verifyOtpUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(AuthErrorCode.OTP_INVALID));

        verify(otpStoragePort).incrementAttempts(eq(tenantId), eq(phoneNumber), eq(900L));
    }

    @Test
    @DisplayName("should return isProfileComplete = false when user has no existing record")
    void should_returnProfileIncomplete_when_userNotFound() {
        when(otpStoragePort.getAttempts(tenantId, phoneNumber)).thenReturn(0);
        when(otpStoragePort.getOtp(tenantId, phoneNumber)).thenReturn(Optional.of("123456"));
        when(userRepository.findByTenantIdAndPhoneNumber(tenantId, phoneNumber)).thenReturn(Optional.empty());

        VerifyOtpRequest request = VerifyOtpRequest.builder().phoneNumber(phoneNumber).otpCode("123456").build();
        VerifyOtpResponse response = verifyOtpUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.isProfileComplete()).isFalse();
        assertThat(response.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(response.getTempAuthToken()).isNotBlank();
        assertThat(response.getUser()).isNull();

        verify(otpStoragePort).clearOtp(tenantId, phoneNumber);
    }

    @Test
    @DisplayName("should return isProfileComplete = true and JWT TokenPair when user exists")
    void should_returnSuccessWithJwt_when_userExists() {
        UUID userId = UUID.randomUUID();
        UUID tierId = UUID.randomUUID();

        UserJpaEntity user = UserJpaEntity.builder()
                .phoneNumber(phoneNumber)
                .email("david.nguyen@ordera.no")
                .fullName("David Nguyen")
                .role("CUSTOMER")
                .build();
        user.setId(userId);
        user.setTenantId(tenantId);

        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentTierId(tierId)
                .currentPoints(120)
                .lifetimePoints(300)
                .build();

        TierDefinitionJpaEntity tier = TierDefinitionJpaEntity.builder()
                .tierName("Silver")
                .build();

        TokenPair tokenPair = TokenPair.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400)
                .build();

        when(otpStoragePort.getAttempts(tenantId, phoneNumber)).thenReturn(0);
        when(otpStoragePort.getOtp(tenantId, phoneNumber)).thenReturn(Optional.of("123456"));
        when(userRepository.findByTenantIdAndPhoneNumber(tenantId, phoneNumber)).thenReturn(Optional.of(user));
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(tierDefinitionRepository.findById(tierId)).thenReturn(Optional.of(tier));
        when(jwtTokenProviderService.generateTokenPair(userId, tenantId, "CUSTOMER")).thenReturn(tokenPair);

        VerifyOtpRequest request = VerifyOtpRequest.builder().phoneNumber(phoneNumber).otpCode("123456").build();
        VerifyOtpResponse response = verifyOtpUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.isProfileComplete()).isTrue();
        assertThat(response.getAccessToken()).isEqualTo("mock-access-token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getFullName()).isEqualTo("David Nguyen");
        assertThat(response.getUser().getTierName()).isEqualTo("Silver");
        assertThat(response.getUser().getCurrentPoints()).isEqualTo(120);

        verify(otpStoragePort).clearOtp(tenantId, phoneNumber);
    }
}

