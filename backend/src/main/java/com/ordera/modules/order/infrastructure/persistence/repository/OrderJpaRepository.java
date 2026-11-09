package com.ordera.modules.order.infrastructure.persistence.repository;

import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Optional<OrderJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<OrderJpaEntity> findByTenantIdAndOrderCode(UUID tenantId, String orderCode);

    List<OrderJpaEntity> findByTenantIdAndUserIdOrderByCreatedAtDesc(UUID tenantId, UUID userId);

    Page<OrderJpaEntity> findByTenantIdAndUserIdAndStatusInOrderByCreatedAtDesc(
            UUID tenantId, UUID userId, List<String> statuses, Pageable pageable);

    List<OrderJpaEntity> findByStoreIdAndStatusOrderByCreatedAtDesc(UUID storeId, String status);

    long countByTenantIdAndStoreIdAndCreatedAtGreaterThanEqual(UUID tenantId, UUID storeId, Instant startOfDay);
}

