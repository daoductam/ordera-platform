package com.ordera.modules.notification.infrastructure.persistence.repository;

import com.ordera.modules.notification.infrastructure.persistence.entity.SystemNotificationJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository cho thông báo hệ thống và chiến dịch.
 * Tuân thủ phân lập dữ liệu đa thương hiệu (Multi-Tenant Isolation qua tenant_id).
 */
@Repository
public interface SystemNotificationJpaRepository extends JpaRepository<SystemNotificationJpaEntity, UUID> {

    Optional<SystemNotificationJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT COUNT(s) FROM SystemNotificationJpaEntity s " +
           "WHERE s.tenantId = :tenantId AND s.active = true " +
           "AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    long countActiveNotifications(@Param("tenantId") UUID tenantId, @Param("now") Instant now);

    @Query("SELECT s.id FROM SystemNotificationJpaEntity s " +
           "WHERE s.tenantId = :tenantId AND s.active = true " +
           "AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    List<UUID> findActiveNotificationIds(@Param("tenantId") UUID tenantId, @Param("now") Instant now);

    @Query("SELECT s FROM SystemNotificationJpaEntity s " +
           "WHERE s.tenantId = :tenantId AND s.active = true " +
           "AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    Page<SystemNotificationJpaEntity> findActiveByTenantId(
            @Param("tenantId") UUID tenantId,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("SELECT s FROM SystemNotificationJpaEntity s " +
           "WHERE s.tenantId = :tenantId AND s.active = true " +
           "AND LOWER(s.category) = LOWER(:category) " +
           "AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    Page<SystemNotificationJpaEntity> findActiveByTenantIdAndCategory(
            @Param("tenantId") UUID tenantId,
            @Param("category") String category,
            @Param("now") Instant now,
            Pageable pageable
    );
}

