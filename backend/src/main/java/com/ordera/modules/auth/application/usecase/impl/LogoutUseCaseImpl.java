package com.ordera.modules.auth.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.application.port.TokenBlacklistPort;
import com.ordera.modules.auth.application.service.JwtTokenProviderService;
import com.ordera.modules.auth.application.usecase.LogoutUseCase;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.presentation.dto.LogoutRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Hiện thực hóa LogoutUseCase theo Orchestrator Pattern.
 * Thu hồi Access Token và Refresh Token vào Redis Blacklist.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final JwtTokenProviderService jwtTokenProviderService;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    public void execute(String authHeader, LogoutRequest request) {
        validateTenant();
        blacklistAccessTokenIfPresent(authHeader);
        blacklistRefreshTokenIfPresent(request);
    }

    private void validateTenant() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(AuthErrorCode.TENANT_HEADER_MISSING);
        }
    }

    private void blacklistAccessTokenIfPresent(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (!token.isBlank()) {
                try {
                    String jti = jwtTokenProviderService.extractJti(token);
                    long remainingTtl = jwtTokenProviderService.getRemainingTtlMs(token);
                    tokenBlacklistPort.blacklistToken(jti, remainingTtl);
                } catch (Exception e) {
                    log.warn("Access token invalid or expired during logout, skipped blacklisting: {}", e.getMessage());
                }
            }
        }
    }

    private void blacklistRefreshTokenIfPresent(LogoutRequest request) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            try {
                String token = request.getRefreshToken().trim();
                String jti = jwtTokenProviderService.extractJti(token);
                long remainingTtl = jwtTokenProviderService.getRemainingTtlMs(token);
                tokenBlacklistPort.blacklistToken(jti, remainingTtl);
            } catch (Exception e) {
                log.warn("Refresh token invalid or expired during logout, skipped blacklisting: {}", e.getMessage());
            }
        }
    }
}

