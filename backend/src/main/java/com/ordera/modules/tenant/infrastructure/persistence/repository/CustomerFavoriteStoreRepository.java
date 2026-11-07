package com.ordera.modules.tenant.infrastructure.persistence.repository;

import com.ordera.modules.tenant.infrastructure.persistence.entity.CustomerFavoriteStoreJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerFavoriteStoreRepository extends JpaRepository<CustomerFavoriteStoreJpaEntity, UUID> {

    List<CustomerFavoriteStoreJpaEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    Optional<CustomerFavoriteStoreJpaEntity> findByTenantIdAndUserIdAndStoreId(UUID tenantId, UUID userId, UUID storeId);

    boolean existsByTenantIdAndUserIdAndStoreId(UUID tenantId, UUID userId, UUID storeId);

    void deleteByTenantIdAndUserIdAndStoreId(UUID tenantId, UUID userId, UUID storeId);
}

