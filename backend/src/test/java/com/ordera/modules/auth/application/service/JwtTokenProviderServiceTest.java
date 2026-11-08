package com.ordera.modules.auth.application.service;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.TokenPair;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtTokenProviderService Unit Tests")
class JwtTokenProviderServiceTest {

    private JwtTokenProviderService jwtService;

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_EXPIRATION_MS = 86400000L; // 24 Hours
    private static final long REFRESH_EXPIRATION_MS = 604800000L; // 7 Days

    @BeforeEach
    void setUp() {
        jwtService = new JwtTokenProviderService(TEST_SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);
    }

    @Test
    @DisplayName("should generate valid TokenPair with expected claims")
    void should_generateValidTokenPair() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String role = "CUSTOMER";

        TokenPair tokenPair = jwtService.generateTokenPair(userId, tenantId, role);

        assertThat(tokenPair).isNotNull();
        assertThat(tokenPair.getAccessToken()).isNotBlank();
        assertThat(tokenPair.getRefreshToken()).isNotBlank();
        assertThat(tokenPair.getTokenType()).isEqualTo("Bearer");
        assertThat(tokenPair.getExpiresIn()).isEqualTo(ACCESS_EXPIRATION_MS / 1000);

        // Verify access token claims
        Claims accessClaims = jwtService.parseClaims(tokenPair.getAccessToken());
        assertThat(accessClaims.getSubject()).isEqualTo(userId.toString());
        assertThat(accessClaims.get("tenantId", String.class)).isEqualTo(tenantId.toString());
        assertThat(accessClaims.get("role", String.class)).isEqualTo(role);
        assertThat(accessClaims.get("type", String.class)).isEqualTo("access");
        assertThat(accessClaims.getId()).isNotBlank();

        // Verify refresh token claims
        Claims refreshClaims = jwtService.parseClaims(tokenPair.getRefreshToken());
        assertThat(refreshClaims.getSubject()).isEqualTo(userId.toString());
        assertThat(refreshClaims.get("tenantId", String.class)).isEqualTo(tenantId.toString());
        assertThat(refreshClaims.get("role", String.class)).isEqualTo(role);
        assertThat(refreshClaims.get("type", String.class)).isEqualTo("refresh");
        assertThat(refreshClaims.getId()).isNotBlank();
    }

    @Test
    @DisplayName("should throw BusinessException(TOKEN_EXPIRED) when token has expired")
    void should_throwException_when_tokenExpired() {
        // Service with negative expiration
        JwtTokenProviderService expiredService = new JwtTokenProviderService(TEST_SECRET, -1000L, -1000L);
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        TokenPair tokenPair = expiredService.generateTokenPair(userId, tenantId, "CUSTOMER");

        assertThatThrownBy(() -> jwtService.parseClaims(tokenPair.getAccessToken()))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(AuthErrorCode.TOKEN_EXPIRED);
                });
    }

    @Test
    @DisplayName("should throw BusinessException(INVALID_CREDENTIALS) when token signature is tampered")
    void should_throwException_when_tokenTampered() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        TokenPair tokenPair = jwtService.generateTokenPair(userId, tenantId, "CUSTOMER");

        String tamperedToken = tokenPair.getAccessToken() + "tampered";

        assertThatThrownBy(() -> jwtService.parseClaims(tamperedToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
                });
    }

    @Test
    @DisplayName("should correctly extract JTI and remaining TTL")
    void should_extractJtiAndRemainingTtl() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        TokenPair tokenPair = jwtService.generateTokenPair(userId, tenantId, "CUSTOMER");

        String jti = jwtService.extractJti(tokenPair.getAccessToken());
        long remainingTtl = jwtService.getRemainingTtlMs(tokenPair.getAccessToken());

        assertThat(jti).isNotBlank();
        assertThat(remainingTtl).isPositive();
        assertThat(remainingTtl).isLessThanOrEqualTo(ACCESS_EXPIRATION_MS);
    }
}

