package com.ordera.modules.tenant.infrastructure.persistence.repository;

import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<StoreJpaEntity, UUID> {

    List<StoreJpaEntity> findByTenantIdAndActive(UUID tenantId, boolean active);

    List<StoreJpaEntity> findByTenantIdAndCityAndActive(UUID tenantId, String city, boolean active);

    List<StoreJpaEntity> findByTenantId(UUID tenantId);

    Optional<StoreJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("SELECT s FROM StoreJpaEntity s WHERE s.tenantId = :tenantId AND s.active = true " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
           "OR LOWER(s.address) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))")
    List<StoreJpaEntity> searchStores(@Param("tenantId") UUID tenantId, @Param("keyword") String keyword);
}
