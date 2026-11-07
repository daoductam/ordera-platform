package com.ordera.modules.tenant.infrastructure.persistence.repository;

import com.ordera.modules.tenant.infrastructure.persistence.entity.KioskDeviceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KioskDeviceRepository extends JpaRepository<KioskDeviceJpaEntity, UUID> {

    List<KioskDeviceJpaEntity> findByTenantIdAndStoreId(UUID tenantId, UUID storeId);

    Optional<KioskDeviceJpaEntity> findByTenantIdAndDeviceCode(UUID tenantId, String deviceCode);
}

