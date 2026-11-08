package com.ordera.modules.loyalty.infrastructure.persistence.repository;

import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TierDefinitionJpaRepository extends JpaRepository<TierDefinitionJpaEntity, UUID> {

    List<TierDefinitionJpaEntity> findByTenantIdOrderByTierOrderAsc(UUID tenantId);

    List<TierDefinitionJpaEntity> findByTenantIdOrderByTierOrderDesc(UUID tenantId);

    Optional<TierDefinitionJpaEntity> findByTenantIdAndTierName(UUID tenantId, String tierName);

    Optional<TierDefinitionJpaEntity> findByTenantIdAndTierOrder(UUID tenantId, int tierOrder);

    Optional<TierDefinitionJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
