package com.ordera.modules.ledger.infrastructure.persistence.repository;

import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerId;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PointsLedgerRepository extends JpaRepository<PointsLedgerJpaEntity, PointsLedgerId> {

    List<PointsLedgerJpaEntity> findByTenantIdAndMemberIdOrderByCreatedAtDesc(UUID tenantId, UUID memberId);

    List<PointsLedgerJpaEntity> findByMemberIdOrderByCreatedAtDesc(UUID memberId);

    Optional<PointsLedgerJpaEntity> findTopByMemberIdOrderByCreatedAtDesc(UUID memberId);

    Optional<PointsLedgerJpaEntity> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
}

