package com.ordera.modules.kiosk.infrastructure.adapter;

import com.ordera.modules.kiosk.domain.constant.KioskConstants;
import com.ordera.modules.kiosk.domain.model.KioskSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter quản lý phiên làm việc Kiosk trên Redis Cache.
 * Áp dụng thời gian hết hạn TTL 30 phút và tự động dọn sạch khi khách nhấn Start Over hoặc Idle Timeout.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisKioskSessionAdapter {

    private final RedissonClient redissonClient;

    /**
     * Lưu trữ phiên làm việc tạm thời của Kiosk với thời gian TTL mặc định.
     */
    public void saveSession(UUID tenantId, String deviceCode, KioskSession session) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(deviceCode, "deviceCode must not be null");
        Objects.requireNonNull(session, "session must not be null");

        String key = buildCacheKey(tenantId, deviceCode);
        try {
            RBucket<KioskSession> bucket = redissonClient.getBucket(key);
            bucket.set(session, Duration.ofMinutes(KioskConstants.SESSION_CACHE_TTL_MINUTES));
            log.info("Saved kiosk session for device: {} (tenant: {})", deviceCode, tenantId);
        } catch (Exception e) {
            log.warn("Failed to save kiosk session to Redis: {}", e.getMessage());
        }
    }

    /**
     * Lấy thông tin phiên làm việc hiện tại của Kiosk.
     */
    public Optional<KioskSession> getSession(UUID tenantId, String deviceCode) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(deviceCode, "deviceCode must not be null");

        String key = buildCacheKey(tenantId, deviceCode);
        try {
            RBucket<KioskSession> bucket = redissonClient.getBucket(key);
            return Optional.ofNullable(bucket.get());
        } catch (Exception e) {
            log.warn("Failed to retrieve kiosk session from Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Xóa sạch dữ liệu phiên làm việc của Kiosk trên Redis (Start Over / Idle Reset).
     */
    public void clearSession(UUID tenantId, String deviceCode) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(deviceCode, "deviceCode must not be null");

        String key = buildCacheKey(tenantId, deviceCode);
        try {
            RBucket<KioskSession> bucket = redissonClient.getBucket(key);
            bucket.delete();
            log.info("Cleared kiosk session for device: {} (tenant: {})", deviceCode, tenantId);
        } catch (Exception e) {
            log.warn("Failed to delete kiosk session from Redis: {}", e.getMessage());
        }
    }

    private String buildCacheKey(UUID tenantId, String deviceCode) {
        return String.format(KioskConstants.SESSION_CACHE_KEY_PATTERN, tenantId, deviceCode);
    }
}

