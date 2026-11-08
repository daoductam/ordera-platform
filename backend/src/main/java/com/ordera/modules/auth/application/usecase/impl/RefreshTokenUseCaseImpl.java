package com.ordera.modules.auth.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.application.port.TokenBlacklistPort;
import com.ordera.modules.auth.application.service.JwtTokenProviderService;
import com.ordera.modules.auth.application.usecase.RefreshTokenUseCase;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.TokenPair;
import com.ordera.modules.auth.presentation.dto.RefreshTokenRequest;
import com.ordera.modules.auth.presentation.dto.RefreshTokenResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Hiện thực hóa RefreshTokenUseCase theo Orchestrator Pattern.
 * Xoay vòng Refresh Token (Token Rotation) nhằm tăng cường bảo mật.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final JwtTokenProviderService jwtTokenProviderService;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    public RefreshTokenResponse execute(RefreshTokenRequest request) {
        validateAndResolveTenant();
        validateRequest(request);

        Claims claims = parseAndValidateRefreshToken(request.getRefreshToken());
        checkBlacklist(claims.getId());
        revokeOldRefreshToken(claims);

        TokenPair newTokenPair = generateNewTokenPair(claims);
        return buildResponse(newTokenPair);
    }

    private void validateAndResolveTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(AuthErrorCode.TENANT_HEADER_MISSING);
        }
    }

    private void validateRequest(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new BusinessException(AuthErrorCode.VALIDATION_FAILED, "Refresh Token không được để trống.");
        }
    }

    private Claims parseAndValidateRefreshToken(String token) {
        Claims claims = jwtTokenProviderService.parseClaims(token);
        String tokenType = claims.get("type", String.class);
        if (!"refresh".equalsIgnoreCase(tokenType)) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS, "Token cung cấp không phải là Refresh Token.");
        }
        return claims;
    }

    private void checkBlacklist(String jti) {
        if (tokenBlacklistPort.isBlacklisted(jti)) {
            throw new BusinessException(AuthErrorCode.TOKEN_REVOKED);
        }
    }

    private void revokeOldRefreshToken(Claims claims) {
        long remainingTtl = jwtTokenProviderService.getRemainingTtlMs(claims.getId());
        if (remainingTtl <= 0) {
            remainingTtl = 604800000L; // Fallback 7 days
        }
        tokenBlacklistPort.blacklistToken(claims.getId(), remainingTtl);
    }

    private TokenPair generateNewTokenPair(Claims claims) {
        UUID userId = UUID.fromString(claims.getSubject());
        UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
        String role = claims.get("role", String.class);

        return jwtTokenProviderService.generateTokenPair(userId, tenantId, role);
    }

    private RefreshTokenResponse buildResponse(TokenPair tokenPair) {
        return RefreshTokenResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .tokenType(tokenPair.getTokenType())
                .expiresIn(tokenPair.getExpiresIn())
                .build();
    }
}
