package com.ordera.modules.catalog.infrastructure.persistence.repository;

import com.ordera.modules.catalog.infrastructure.persistence.entity.PromotionBannerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromotionBannerRepository extends JpaRepository<PromotionBannerJpaEntity, UUID> {

    @Query("SELECT b FROM PromotionBannerJpaEntity b WHERE b.tenantId = :tenantId AND b.active = true " +
           "AND (b.storeId = :storeId OR b.storeId IS NULL) " +
           "ORDER BY b.displayOrder ASC")
    List<PromotionBannerJpaEntity> findActiveBanners(@Param("tenantId") UUID tenantId, @Param("storeId") UUID storeId);

    List<PromotionBannerJpaEntity> findByTenantIdAndActiveOrderByDisplayOrderAsc(UUID tenantId, boolean active);
}
