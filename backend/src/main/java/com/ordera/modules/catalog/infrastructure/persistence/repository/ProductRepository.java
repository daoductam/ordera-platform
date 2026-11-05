package com.ordera.modules.catalog.infrastructure.persistence.repository;

import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductJpaEntity, UUID> {

    List<ProductJpaEntity> findByTenantIdAndAvailable(UUID tenantId, boolean available);

    List<ProductJpaEntity> findByCategoryIdAndAvailable(UUID categoryId, boolean available);

    List<ProductJpaEntity> findByTenantIdAndNameContainingIgnoreCase(UUID tenantId, String keyword);

    List<ProductJpaEntity> findByTenantId(UUID tenantId);
}
