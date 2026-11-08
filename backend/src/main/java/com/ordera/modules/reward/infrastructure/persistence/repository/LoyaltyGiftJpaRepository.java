package com.ordera.modules.reward.infrastructure.persistence.repository;

import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyGiftJpaRepository extends JpaRepository<LoyaltyGiftJpaEntity, UUID> {

    List<LoyaltyGiftJpaEntity> findByTenantIdAndActive(UUID tenantId, boolean active);

    Optional<LoyaltyGiftJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}

