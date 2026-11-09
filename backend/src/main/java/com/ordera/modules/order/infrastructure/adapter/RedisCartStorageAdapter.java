package com.ordera.modules.order.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordera.modules.order.domain.constant.OrderConstants;
import com.ordera.modules.order.domain.model.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Adapter quản lý giỏ hàng và khóa Idempotency trên Redis Cache.
 * Phân lập key theo tenantId và userId / cartId.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCartStorageAdapter {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private String buildCartUserKey(UUID tenantId, UUID userId) {
        return OrderConstants.REDIS_CART_PREFIX + tenantId + ":user:" + userId;
    }

    private String buildCartIdKey(UUID tenantId, UUID cartId) {
        return OrderConstants.REDIS_CART_PREFIX + tenantId + ":id:" + cartId;
    }

    private String buildIdempotencyKey(UUID tenantId, String idempotencyKey) {
        return OrderConstants.REDIS_IDEMPOTENCY_PREFIX + tenantId + ":" + idempotencyKey;
    }

    /**
     * Lưu trữ giỏ hàng vào Redis với TTL 7 ngày (CART_TTL_DAYS).
     */
    public void saveCart(Cart cart) {
        if (cart == null || cart.getId() == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(cart);
            Duration ttl = Duration.ofDays(OrderConstants.CART_TTL_DAYS);

            String idKey = buildCartIdKey(cart.getTenantId(), cart.getId());
            redisTemplate.opsForValue().set(idKey, json, ttl);

            if (cart.getUserId() != null) {
                String userKey = buildCartUserKey(cart.getTenantId(), cart.getUserId());
                redisTemplate.opsForValue().set(userKey, json, ttl);
            }
            log.debug("Saved cart {} to Redis for tenant {}", cart.getId(), cart.getTenantId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cart {} to JSON: {}", cart.getId(), e.getMessage());
        }
    }

    /**
     * Lấy giỏ hàng theo cartId từ Redis.
     */
    public Optional<Cart> getCartById(UUID tenantId, UUID cartId) {
        String key = buildCartIdKey(tenantId, cartId);
        return getCartFromKey(key);
    }

    /**
     * Lấy giỏ hàng theo userId từ Redis.
     */
    public Optional<Cart> getCartByUserId(UUID tenantId, UUID userId) {
        String key = buildCartUserKey(tenantId, userId);
        return getCartFromKey(key);
    }

    private Optional<Cart> getCartFromKey(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            Cart cart = objectMapper.readValue(json, Cart.class);
            return Optional.ofNullable(cart);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cart from Redis key {}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Xoá giỏ hàng khỏi Redis cache sau khi chốt đơn thành công.
     */
    public void deleteCart(UUID tenantId, UUID cartId, UUID userId) {
        String idKey = buildCartIdKey(tenantId, cartId);
        redisTemplate.delete(idKey);
        if (userId != null) {
            String userKey = buildCartUserKey(tenantId, userId);
            redisTemplate.delete(userKey);
        }
        log.debug("Deleted cart {} from Redis cache for tenant {}", cartId, tenantId);
    }

    /**
     * Lưu trữ Idempotency Key với TTL 24 giờ (IDEMPOTENCY_KEY_TTL_HOURS).
     */
    public boolean saveIdempotencyRecord(UUID tenantId, String idempotencyKey, String orderId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return true;
        }
        String key = buildIdempotencyKey(tenantId, idempotencyKey);
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key, orderId, OrderConstants.IDEMPOTENCY_KEY_TTL_HOURS, TimeUnit.HOURS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * Kiểm tra xem Idempotency Key đã tồn tại chưa.
     */
    public Optional<String> getIdempotentOrderId(UUID tenantId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        String key = buildIdempotencyKey(tenantId, idempotencyKey);
        String val = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(val);
    }
}
