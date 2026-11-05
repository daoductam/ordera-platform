package com.ordera.modules.tenant.infrastructure.persistence.repository;

import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<StoreJpaEntity, UUID> {

    List<StoreJpaEntity> findByTenantIdAndActive(UUID tenantId, boolean active);

    List<StoreJpaEntity> findByTenantIdAndCityAndActive(UUID tenantId, String city, boolean active);

    List<StoreJpaEntity> findByTenantId(UUID tenantId);
}

