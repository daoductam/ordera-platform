package com.ordera.modules.auth.infrastructure.adapter;

import com.ordera.modules.auth.application.port.OtpStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Hiện thực hóa OtpStoragePort sử dụng Redis 7 và StringRedisTemplate.
 * Tất cả các khóa đều được phân lập theo tiền tố tenantId.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOtpStorageAdapter implements OtpStoragePort {

    private final StringRedisTemplate redisTemplate;

    private static final String OTP_PREFIX = "auth:otp:";
    private static final String COOLDOWN_PREFIX = "auth:otp:cooldown:";
    private static final String ATTEMPTS_PREFIX = "auth:otp:attempts:";
    private static final int MAX_ATTEMPTS = 5;

    private String buildOtpKey(UUID tenantId, String recipient) {
        return OTP_PREFIX + tenantId + ":" + recipient;
    }

    private String buildCooldownKey(UUID tenantId, String recipient) {
        return COOLDOWN_PREFIX + tenantId + ":" + recipient;
    }

    private String buildAttemptsKey(UUID tenantId, String recipient) {
        return ATTEMPTS_PREFIX + tenantId + ":" + recipient;
    }

    @Override
    public void saveOtp(UUID tenantId, String recipient, String otpCode, long ttlSeconds) {
        String otpKey = buildOtpKey(tenantId, recipient);
        redisTemplate.opsForValue().set(otpKey, otpCode, ttlSeconds, TimeUnit.SECONDS);
        setCooldown(tenantId, recipient, ttlSeconds);
        log.debug("Saved OTP for tenant {} recipient {}", tenantId, recipient);
    }

    @Override
    public Optional<String> getOtp(UUID tenantId, String recipient) {
        String key = buildOtpKey(tenantId, recipient);
        String val = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(val);
    }

    @Override
    public boolean isCooldownActive(UUID tenantId, String recipient) {
        String key = buildCooldownKey(tenantId, recipient);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void setCooldown(UUID tenantId, String recipient, long cooldownSeconds) {
        String key = buildCooldownKey(tenantId, recipient);
        redisTemplate.opsForValue().set(key, "active", cooldownSeconds, TimeUnit.SECONDS);
    }

    @Override
    public long getCooldownRemainingSeconds(UUID tenantId, String recipient) {
        String key = buildCooldownKey(tenantId, recipient);
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return (expire != null && expire > 0) ? expire : 0L;
    }

    @Override
    public int incrementAttempts(UUID tenantId, String recipient, long lockTtlSeconds) {
        String key = buildAttemptsKey(tenantId, recipient);
        Long count = redisTemplate.opsForValue().increment(key);
        int currentAttempts = (count != null) ? count.intValue() : 1;
        if (currentAttempts == 1) {
            // First failed attempt, set initial TTL (e.g. 120s or lock duration)
            redisTemplate.expire(key, Math.max(120, lockTtlSeconds), TimeUnit.SECONDS);
        } else if (currentAttempts >= MAX_ATTEMPTS) {
            // Exceeded threshold: lock for lockTtlSeconds (e.g. 900s / 15 mins)
            redisTemplate.expire(key, lockTtlSeconds, TimeUnit.SECONDS);
        }
        return currentAttempts;
    }

    @Override
    public int getAttempts(UUID tenantId, String recipient) {
        String key = buildAttemptsKey(tenantId, recipient);
        String val = redisTemplate.opsForValue().get(key);
        if (val == null) {
            return 0;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void clearOtp(UUID tenantId, String recipient) {
        redisTemplate.delete(buildOtpKey(tenantId, recipient));
        redisTemplate.delete(buildAttemptsKey(tenantId, recipient));
    }
}

