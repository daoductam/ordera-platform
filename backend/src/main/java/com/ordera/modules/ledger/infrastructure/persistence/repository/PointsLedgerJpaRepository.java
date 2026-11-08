package com.ordera.modules.ledger.infrastructure.persistence.repository;

import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerId;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PointsLedgerJpaRepository extends JpaRepository<PointsLedgerJpaEntity, PointsLedgerId> {

    Page<PointsLedgerJpaEntity> findByTenantIdAndMemberIdOrderByCreatedAtDesc(UUID tenantId, UUID memberId, Pageable pageable);
}

