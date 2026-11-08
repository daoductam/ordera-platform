package com.ordera.modules.auth.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.application.port.TokenBlacklistPort;
import com.ordera.modules.auth.application.service.JwtTokenProviderService;
import com.ordera.modules.auth.application.usecase.impl.RefreshTokenUseCaseImpl;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.TokenPair;
import com.ordera.modules.auth.presentation.dto.RefreshTokenRequest;
import com.ordera.modules.auth.presentation.dto.RefreshTokenResponse;
import io.jsonwebtoken.Claims;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenUseCase Unit Tests")
class RefreshTokenUseCaseTest {

    @Mock
    private JwtTokenProviderService jwtTokenProviderService;

    @Mock
    private TokenBlacklistPort tokenBlacklistPort;

    @Mock
    private Claims claims;

    private RefreshTokenUseCaseImpl refreshTokenUseCase;
    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
        refreshTokenUseCase = new RefreshTokenUseCaseImpl(jwtTokenProviderService, tokenBlacklistPort);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should throw TOKEN_REVOKED when refresh token is blacklisted")
    void should_throwException_when_tokenBlacklisted() {
        String token = "valid-refresh-token";
        String jti = "revoked-jti";

        when(jwtTokenProviderService.parseClaims(token)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(claims.getId()).thenReturn(jti);
        when(tokenBlacklistPort.isBlacklisted(jti)).thenReturn(true);

        RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken(token).build();

        assertThatThrownBy(() -> refreshTokenUseCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(AuthErrorCode.TOKEN_REVOKED));
    }

    @Test
    @DisplayName("should rotate token successfully when refresh token is valid")
    void should_rotateTokensSuccessfully() {
        String token = "valid-refresh-token";
        String jti = "valid-jti";

        when(jwtTokenProviderService.parseClaims(token)).thenReturn(claims);
        when(claims.get("type", String.class)).thenReturn("refresh");
        when(claims.getId()).thenReturn(jti);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(claims.get("tenantId", String.class)).thenReturn(tenantId.toString());
        when(claims.get("role", String.class)).thenReturn("CUSTOMER");
        when(tokenBlacklistPort.isBlacklisted(jti)).thenReturn(false);
        when(jwtTokenProviderService.getRemainingTtlMs(jti)).thenReturn(500000L);

        TokenPair newPair = TokenPair.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(86400)
                .build();
        when(jwtTokenProviderService.generateTokenPair(userId, tenantId, "CUSTOMER")).thenReturn(newPair);

        RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken(token).build();
        RefreshTokenResponse response = refreshTokenUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");

        verify(tokenBlacklistPort).blacklistToken(eq(jti), anyLong());
    }
}
