package com.ordera.modules.auth.infrastructure.adapter;

import com.ordera.modules.auth.application.port.TokenBlacklistPort;
import com.ordera.modules.auth.domain.constant.AuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Hiện thực hóa TokenBlacklistPort sử dụng Redis 7.
 * Lưu trữ danh sách đen các JWT ID (jti) đã bị thu hồi khi người dùng bấm Đăng xuất.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistAdapter implements TokenBlacklistPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklistToken(String jti, long remainingTtlMs) {
        if (jti == null || jti.isBlank() || remainingTtlMs <= 0) {
            return;
        }
        String key = AuthConstants.REDIS_BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "REVOKED", remainingTtlMs, TimeUnit.MILLISECONDS);
        log.info("Blacklisted JWT jti {} for remaining TTL {} ms", jti, remainingTtlMs);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        String key = AuthConstants.REDIS_BLACKLIST_PREFIX + jti;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}

