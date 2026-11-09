package com.ordera.modules.notification.infrastructure.persistence.repository;

import com.ordera.modules.notification.infrastructure.persistence.entity.UserNotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Spring Data JPA Repository quản lý trạng thái đọc thông báo của hội viên (Fan-out on Read).
 * Bắt buộc phân lập theo tenant_id và user_id.
 */
@Repository
public interface UserNotificationJpaRepository extends JpaRepository<UserNotificationJpaEntity, UUID> {

    Optional<UserNotificationJpaEntity> findByTenantIdAndUserIdAndNotificationId(
            UUID tenantId, UUID userId, UUID notificationId
    );

    List<UserNotificationJpaEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    @Query("SELECT COUNT(u) FROM UserNotificationJpaEntity u " +
           "WHERE u.tenantId = :tenantId AND u.userId = :userId AND u.read = true")
    long countReadNotifications(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId);

    @Query("SELECT u.notificationId FROM UserNotificationJpaEntity u " +
           "WHERE u.tenantId = :tenantId AND u.userId = :userId AND u.read = true")
    Set<UUID> findReadNotificationIds(@Param("tenantId") UUID tenantId, @Param("userId") UUID userId);
}

