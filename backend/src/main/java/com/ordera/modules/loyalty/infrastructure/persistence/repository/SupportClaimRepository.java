package com.ordera.modules.loyalty.infrastructure.persistence.repository;

import com.ordera.modules.loyalty.infrastructure.persistence.entity.SupportClaimJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupportClaimRepository extends JpaRepository<SupportClaimJpaEntity, UUID> {

    List<SupportClaimJpaEntity> findByTenantIdAndStatus(UUID tenantId, String status);

    List<SupportClaimJpaEntity> findByMemberIdOrderByCreatedAtDesc(UUID memberId);

    Optional<SupportClaimJpaEntity> findByTenantIdAndReceiptCode(UUID tenantId, String receiptCode);
}
