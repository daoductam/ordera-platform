package com.ordera.modules.reward.infrastructure.persistence.repository;

import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoucherJpaRepository extends JpaRepository<VoucherJpaEntity, UUID> {

    List<VoucherJpaEntity> findByTenantIdAndActive(UUID tenantId, boolean active);

    Optional<VoucherJpaEntity> findByTenantIdAndCode(UUID tenantId, String code);

    Optional<VoucherJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<VoucherJpaEntity> findByTenantIdAndActiveAndValidUntilAfter(UUID tenantId, boolean active, Instant now);
}
