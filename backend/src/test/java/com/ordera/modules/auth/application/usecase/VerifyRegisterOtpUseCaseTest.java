package com.ordera.modules.auth.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.application.port.OtpStoragePort;
import com.ordera.modules.auth.application.service.JwtTokenProviderService;
import com.ordera.modules.auth.application.usecase.impl.VerifyRegisterOtpUseCaseImpl;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.TokenPair;
import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.ordera.modules.auth.infrastructure.persistence.repository.UserRepository;
import com.ordera.modules.auth.presentation.dto.VerifyRegisterOtpRequest;
import com.ordera.modules.auth.presentation.dto.VerifyRegisterOtpResponse;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyRegisterOtpUseCase Unit Tests")
class VerifyRegisterOtpUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberProfileRepository memberProfileRepository;

    @Mock
    private TierDefinitionRepository tierDefinitionRepository;

    @Mock
    private OtpStoragePort otpStoragePort;

    @Mock
    private JwtTokenProviderService jwtTokenProviderService;

    private VerifyRegisterOtpUseCaseImpl verifyRegisterOtpUseCase;
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
        verifyRegisterOtpUseCase = new VerifyRegisterOtpUseCaseImpl(
                userRepository, memberProfileRepository, tierDefinitionRepository,
                otpStoragePort, jwtTokenProviderService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should throw OTP_INVALID when OTP does not match")
    void should_throwException_when_otpInvalid() {
        VerifyRegisterOtpRequest request = VerifyRegisterOtpRequest.builder()
                .phoneNumber("+4797362748")
                .email("abytran@ordera.no")
                .otpCode("111111")
                .build();

        when(otpStoragePort.getOtp(tenantId, request.getEmail())).thenReturn(Optional.of("222222"));
        when(otpStoragePort.incrementAttempts(eq(tenantId), eq(request.getEmail()), anyLong())).thenReturn(1);

        assertThatThrownBy(() -> verifyRegisterOtpUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(AuthErrorCode.OTP_INVALID));

        verify(otpStoragePort).incrementAttempts(eq(tenantId), eq(request.getEmail()), eq(900L));
    }

    @Test
    @DisplayName("should throw OTP_MAX_ATTEMPTS_EXCEEDED when attempts reach or exceed 5")
    void should_throwException_when_maxAttemptsExceeded() {
        VerifyRegisterOtpRequest request = VerifyRegisterOtpRequest.builder()
                .phoneNumber("+4797362748")
                .email("abytran@ordera.no")
                .otpCode("111111")
                .build();

        when(otpStoragePort.getAttempts(tenantId, request.getEmail())).thenReturn(5);

        assertThatThrownBy(() -> verifyRegisterOtpUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED));
    }

    @Test
    @DisplayName("should successfully register user and create member profile with Tier New and 0 points")
    void should_registerUserAndCreateMemberProfile_when_validOtp() {
        VerifyRegisterOtpRequest request = VerifyRegisterOtpRequest.builder()
                .phoneNumber("+4797362748")
                .email("abytran@ordera.no")
                .fullName("ABY TRAN")
                .dateOfBirth(LocalDate.of(2000, 8, 23))
                .otpCode("387505")
                .build();

        UUID tierId = UUID.randomUUID();
        TierDefinitionJpaEntity newTier = TierDefinitionJpaEntity.builder()
                .tierName("New")
                .tierOrder(1)
                .themeColorHex("#1EB759")
                .minPointsRequired(0)
                .build();
        newTier.setId(tierId);

        TokenPair tokenPair = TokenPair.builder()
                .accessToken("mock-reg-access")
                .refreshToken("mock-reg-refresh")
                .tokenType("Bearer")
                .expiresIn(86400)
                .build();

        when(otpStoragePort.getOtp(tenantId, request.getEmail())).thenReturn(Optional.of("387505"));
        when(userRepository.existsByTenantIdAndPhoneNumber(tenantId, request.getPhoneNumber())).thenReturn(false);
        when(userRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())).thenReturn(false);
        when(tierDefinitionRepository.findByTenantIdAndTierOrder(tenantId, 1)).thenReturn(Optional.of(newTier));

        when(userRepository.save(any(UserJpaEntity.class))).thenAnswer(invocation -> {
            UserJpaEntity saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        when(memberProfileRepository.save(any(MemberProfileJpaEntity.class))).thenAnswer(invocation -> {
            MemberProfileJpaEntity saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        when(jwtTokenProviderService.generateTokenPair(any(UUID.class), eq(tenantId), eq("CUSTOMER")))
                .thenReturn(tokenPair);

        VerifyRegisterOtpResponse response = verifyRegisterOtpUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock-reg-access");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getFullName()).isEqualTo("ABY TRAN");
        assertThat(response.getUser().getEmail()).isEqualTo("abytran@ordera.no");
        assertThat(response.getUser().getDateOfBirth()).isEqualTo(LocalDate.of(2000, 8, 23));
        assertThat(response.getLoyalty()).isNotNull();
        assertThat(response.getLoyalty().getTierName()).isEqualTo("New");
        assertThat(response.getLoyalty().getCurrentPoints()).isEqualTo(0);
        assertThat(response.getLoyalty().getLifetimePoints()).isEqualTo(0);
        assertThat(response.getLoyalty().getPointsToNextTier()).isEqualTo(200);

        verify(otpStoragePort).clearOtp(tenantId, request.getEmail());
        verify(userRepository).save(any(UserJpaEntity.class));
        verify(memberProfileRepository).save(any(MemberProfileJpaEntity.class));
    }
}

