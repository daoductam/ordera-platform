package com.ordera.modules.auth.application.service;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.auth.domain.exception.AuthErrorCode;
import com.ordera.modules.auth.domain.model.TokenPair;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Service cấp phát và thẩm định JSON Web Token (JWT) sử dụng thuật toán ký HMAC-SHA512.
 */
@Service
public class JwtTokenProviderService {

    private final SecretKey signingKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProviderService(
            @Value("${ordera.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret,
            @Value("${ordera.jwt.expiration-ms:86400000}") long accessExpirationMs,
            @Value("${ordera.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.signingKey = initSigningKey(secret);
    }

    private SecretKey initSigningKey(String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] keyBytes = md.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 algorithm not available", e);
        }
    }

    /**
     * Cấp phát cặp Access Token và Refresh Token cho người dùng đã xác thực.
     */
    public TokenPair generateTokenPair(UUID userId, UUID tenantId, String role) {
        Instant now = Instant.now();
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        String accessToken = Jwts.builder()
                .id(accessJti)
                .subject(userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpirationMs)))
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();

        String refreshToken = Jwts.builder()
                .id(refreshJti)
                .subject(userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("role", role)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshExpirationMs)))
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpirationMs / 1000)
                .build();
    }

    /**
     * Giải mã và kiểm tra tính hợp lệ của JWT token.
     *
     * @param token Chuỗi JWT token
     * @return Đối tượng Claims nếu token hợp lệ
     * @throws BusinessException nếu token hết hạn hoặc chữ ký bị giả mạo
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(AuthErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }
    }

    /**
     * Trích xuất JWT ID (jti) phục vụ đưa vào danh sách đen khi Logout.
     */
    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    /**
     * Trích xuất User ID từ JWT token.
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    /**
     * Trích xuất Tenant ID từ JWT token.
     */
    public UUID extractTenantId(String token) {
        String tenantIdStr = parseClaims(token).get("tenantId", String.class);
        return UUID.fromString(tenantIdStr);
    }

    /**
     * Lấy thời gian sống còn lại của token (mili-giây).
     */
    public long getRemainingTtlMs(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}
