package com.ordera.modules.kiosk.infrastructure.persistence.repository;

import com.ordera.modules.kiosk.infrastructure.persistence.entity.KioskDeviceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository quản lý thiết bị Kiosk trạm.
 * Bắt buộc phân lập theo tenant_id để chống lỗ hổng IDOR.
 */
@Repository
public interface KioskDeviceJpaRepository extends JpaRepository<KioskDeviceJpaEntity, UUID> {

    Optional<KioskDeviceJpaEntity> findByTenantIdAndDeviceCode(UUID tenantId, String deviceCode);

    List<KioskDeviceJpaEntity> findByTenantIdAndStoreId(UUID tenantId, UUID storeId);

    List<KioskDeviceJpaEntity> findByTenantIdAndStoreIdAndActive(UUID tenantId, UUID storeId, boolean active);

    boolean existsByTenantIdAndDeviceCode(UUID tenantId, String deviceCode);
}

