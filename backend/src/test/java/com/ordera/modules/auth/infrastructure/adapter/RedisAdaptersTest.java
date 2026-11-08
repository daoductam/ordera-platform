package com.ordera.modules.auth.infrastructure.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Redis Adapters Unit Tests")
class RedisAdaptersTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisOtpStorageAdapter otpStorageAdapter;
    private RedisTokenBlacklistAdapter blacklistAdapter;

    private final UUID tenantId = UUID.randomUUID();
    private final String recipient = "+4797362748";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        otpStorageAdapter = new RedisOtpStorageAdapter(redisTemplate);
        blacklistAdapter = new RedisTokenBlacklistAdapter(redisTemplate);
    }

    @Test
    @DisplayName("should save OTP with TTL and set cooldown in Redis")
    void should_saveOtpAndCooldown() {
        otpStorageAdapter.saveOtp(tenantId, recipient, "123456", 120);

        verify(valueOperations).set("auth:otp:" + tenantId + ":" + recipient, "123456", 120, TimeUnit.SECONDS);
        verify(valueOperations).set("auth:otp:cooldown:" + tenantId + ":" + recipient, "active", 120, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("should get OTP from Redis when present")
    void should_getOtp() {
        when(valueOperations.get("auth:otp:" + tenantId + ":" + recipient)).thenReturn("849201");

        Optional<String> otp = otpStorageAdapter.getOtp(tenantId, recipient);

        assertThat(otp).isPresent().contains("849201");
    }

    @Test
    @DisplayName("should return true when cooldown key exists")
    void should_checkCooldownActive() {
        when(redisTemplate.hasKey("auth:otp:cooldown:" + tenantId + ":" + recipient)).thenReturn(Boolean.TRUE);

        boolean active = otpStorageAdapter.isCooldownActive(tenantId, recipient);

        assertThat(active).isTrue();
    }

    @Test
    @DisplayName("should increment attempts and set lock when >= 5")
    void should_incrementAttemptsAndLock() {
        when(valueOperations.increment("auth:otp:attempts:" + tenantId + ":" + recipient)).thenReturn(5L);

        int attempts = otpStorageAdapter.incrementAttempts(tenantId, recipient, 900);

        assertThat(attempts).isEqualTo(5);
        verify(redisTemplate).expire("auth:otp:attempts:" + tenantId + ":" + recipient, 900, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("should clear OTP and attempts from Redis")
    void should_clearOtp() {
        otpStorageAdapter.clearOtp(tenantId, recipient);

        verify(redisTemplate).delete("auth:otp:" + tenantId + ":" + recipient);
        verify(redisTemplate).delete("auth:otp:attempts:" + tenantId + ":" + recipient);
    }

    @Test
    @DisplayName("should blacklist token with remaining TTL in ms")
    void should_blacklistToken() {
        String jti = "test-jti-12345";
        blacklistAdapter.blacklistToken(jti, 3600000L);

        verify(valueOperations).set("auth:blacklist:" + jti, "REVOKED", 3600000L, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("should return true when token is in blacklist")
    void should_checkTokenBlacklisted() {
        String jti = "test-jti-12345";
        when(redisTemplate.hasKey("auth:blacklist:" + jti)).thenReturn(Boolean.TRUE);

        boolean blacklisted = blacklistAdapter.isBlacklisted(jti);

        assertThat(blacklisted).isTrue();
    }
}
