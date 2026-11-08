package com.ordera.modules.loyalty.infrastructure.persistence.repository;

import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberProfileJpaRepository extends JpaRepository<MemberProfileJpaEntity, UUID> {

    Optional<MemberProfileJpaEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    Optional<MemberProfileJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<MemberProfileJpaEntity> findByTenantIdAndBarcodeToken(UUID tenantId, String barcodeToken);

    boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);

    List<MemberProfileJpaEntity> findByTenantIdAndCurrentTierId(UUID tenantId, UUID currentTierId);
}

