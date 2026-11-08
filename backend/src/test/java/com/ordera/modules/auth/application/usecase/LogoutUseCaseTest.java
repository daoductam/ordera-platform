package com.ordera.modules.auth.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.auth.application.port.TokenBlacklistPort;
import com.ordera.modules.auth.application.service.JwtTokenProviderService;
import com.ordera.modules.auth.application.usecase.impl.LogoutUseCaseImpl;
import com.ordera.modules.auth.presentation.dto.LogoutRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutUseCase Unit Tests")
class LogoutUseCaseTest {

    @Mock
    private JwtTokenProviderService jwtTokenProviderService;

    @Mock
    private TokenBlacklistPort tokenBlacklistPort;

    private LogoutUseCaseImpl logoutUseCase;
    private final UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
        logoutUseCase = new LogoutUseCaseImpl(jwtTokenProviderService, tokenBlacklistPort);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should blacklist access token from Bearer authorization header")
    void should_blacklistAccessTokenFromHeader() {
        String authHeader = "Bearer mock-access-token";
        String jti = "access-jti-123";

        when(jwtTokenProviderService.extractJti("mock-access-token")).thenReturn(jti);
        when(jwtTokenProviderService.getRemainingTtlMs("mock-access-token")).thenReturn(7200000L);

        logoutUseCase.execute(authHeader, new LogoutRequest());

        verify(tokenBlacklistPort).blacklistToken(jti, 7200000L);
    }

    @Test
    @DisplayName("should blacklist both access and refresh token when both provided")
    void should_blacklistBothTokens() {
        String authHeader = "Bearer mock-access-token";
        String accessJti = "access-jti";
        String refreshJti = "refresh-jti";
        LogoutRequest request = LogoutRequest.builder().refreshToken("mock-refresh-token").build();

        when(jwtTokenProviderService.extractJti("mock-access-token")).thenReturn(accessJti);
        when(jwtTokenProviderService.getRemainingTtlMs("mock-access-token")).thenReturn(7200000L);
        when(jwtTokenProviderService.extractJti("mock-refresh-token")).thenReturn(refreshJti);
        when(jwtTokenProviderService.getRemainingTtlMs("mock-refresh-token")).thenReturn(604800000L);

        logoutUseCase.execute(authHeader, request);

        verify(tokenBlacklistPort).blacklistToken(accessJti, 7200000L);
        verify(tokenBlacklistPort).blacklistToken(refreshJti, 604800000L);
    }
}

