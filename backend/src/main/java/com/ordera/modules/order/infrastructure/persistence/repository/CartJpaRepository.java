package com.ordera.modules.order.infrastructure.persistence.repository;

import com.ordera.modules.order.infrastructure.persistence.entity.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartJpaRepository extends JpaRepository<CartJpaEntity, UUID> {

    Optional<CartJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<CartJpaEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    Optional<CartJpaEntity> findByTenantIdAndStoreIdAndUserId(UUID tenantId, UUID storeId, UUID userId);

    void deleteByIdAndTenantId(UUID id, UUID tenantId);
}
