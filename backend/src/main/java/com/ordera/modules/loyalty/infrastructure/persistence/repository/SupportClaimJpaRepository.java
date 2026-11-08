package com.ordera.modules.loyalty.infrastructure.persistence.repository;

import com.ordera.modules.loyalty.infrastructure.persistence.entity.SupportClaimJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupportClaimJpaRepository extends JpaRepository<SupportClaimJpaEntity, UUID> {

    boolean existsByTenantIdAndReceiptCode(UUID tenantId, String receiptCode);

    boolean existsByTenantIdAndMemberIdAndStatus(UUID tenantId, UUID memberId, String status);

    Page<SupportClaimJpaEntity> findByTenantIdAndMemberIdOrderByCreatedAtDesc(UUID tenantId, UUID memberId, Pageable pageable);

    Optional<SupportClaimJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}

