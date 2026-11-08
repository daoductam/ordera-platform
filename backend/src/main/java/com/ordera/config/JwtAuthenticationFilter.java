package com.ordera.config;

import com.ordera.common.context.TenantContext;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.auth.application.port.TokenBlacklistPort;
import com.ordera.modules.auth.application.service.JwtTokenProviderService;
import com.ordera.modules.auth.domain.constant.AuthConstants;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Filter xác thực JSON Web Token (JWT) và thiết lập SecurityContext cho mỗi HTTP request.
 * Hỗ trợ xác thực qua Bearer Token hoặc X-User-ID (chế độ kiểm thử dev/swagger).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProviderService jwtTokenProviderService;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                if (!token.isEmpty()) {
                    processBearerToken(token);
                }
            } else {
                // Hỗ trợ kiểm thử trực tiếp trên Swagger UI / Dev qua Header X-User-ID
                String userIdHeader = request.getHeader("X-User-ID");
                if (userIdHeader != null && !userIdHeader.isBlank()) {
                    processUserIdHeader(userIdHeader);
                }
            }
        } catch (Exception e) {
            log.warn("Could not set user authentication in security context: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void processBearerToken(String token) {
        String jti = jwtTokenProviderService.extractJti(token);
        if (tokenBlacklistPort.isBlacklisted(jti)) {
            log.warn("Rejected blacklisted JWT token jti: {}", jti);
            return;
        }

        Claims claims = jwtTokenProviderService.parseClaims(token);
        String subject = claims.getSubject();
        String role = claims.get(AuthConstants.CLAIM_ROLE, String.class);
        String tenantIdStr = claims.get(AuthConstants.CLAIM_TENANT_ID, String.class);

        if (subject != null) {
            List<SimpleGrantedAuthority> authorities = role != null
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    : Collections.emptyList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(subject, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (tenantIdStr != null && TenantContext.getTenantId() == null) {
                SecurityUtils.parseUuid(tenantIdStr).ifPresent(TenantContext::setTenantId);
            }
        }
    }

    private void processUserIdHeader(String userIdHeader) {
        SecurityUtils.parseUuid(userIdHeader).ifPresent(userId -> {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId.toString(), null,
                            List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });
    }
}
