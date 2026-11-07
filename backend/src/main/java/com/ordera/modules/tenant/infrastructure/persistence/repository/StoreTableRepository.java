package com.ordera.modules.tenant.infrastructure.persistence.repository;

import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreTableJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreTableRepository extends JpaRepository<StoreTableJpaEntity, UUID> {

    List<StoreTableJpaEntity> findByStoreId(UUID storeId);

    List<StoreTableJpaEntity> findByTenantIdAndStoreId(UUID tenantId, UUID storeId);

    Optional<StoreTableJpaEntity> findByStoreIdAndTableNumber(UUID storeId, String tableNumber);

    Optional<StoreTableJpaEntity> findByTenantIdAndStoreIdAndTableNumber(UUID tenantId, UUID storeId, String tableNumber);

    Optional<StoreTableJpaEntity> findByQrCodeToken(String qrCodeToken);

    Optional<StoreTableJpaEntity> findByTenantIdAndQrCodeToken(UUID tenantId, String qrCodeToken);

    @Query("SELECT t FROM StoreTableJpaEntity t WHERE t.tenantId = :tenantId AND LOWER(t.qrCodeToken) = LOWER(:qrCodeToken)")
    Optional<StoreTableJpaEntity> findByTenantIdAndQrCodeTokenIgnoreCase(@Param("tenantId") UUID tenantId, @Param("qrCodeToken") String qrCodeToken);
}
