package com.ordera.modules.auth.infrastructure.persistence.repository;

import com.ordera.modules.auth.infrastructure.persistence.entity.OtpAuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpAuditLogRepository extends JpaRepository<OtpAuditLogJpaEntity, UUID> {

    Optional<OtpAuditLogJpaEntity> findTopByTenantIdAndRecipientOrderByCreatedAtDesc(UUID tenantId, String recipient);

    long countByTenantIdAndRecipientAndCreatedAtAfter(UUID tenantId, String recipient, Instant after);
}
