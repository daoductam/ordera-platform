package com.ordera.modules.order.infrastructure.persistence.repository;

import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Optional<OrderJpaEntity> findByTenantIdAndOrderCode(UUID tenantId, String orderCode);

    List<OrderJpaEntity> findByTenantIdAndUserIdOrderByCreatedAtDesc(UUID tenantId, UUID userId);

    List<OrderJpaEntity> findByStoreIdAndStatusOrderByCreatedAtDesc(UUID storeId, String status);

    List<OrderJpaEntity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
