package com.ordera.modules.catalog.infrastructure.persistence.repository;

import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductJpaEntity, UUID> {

    List<ProductJpaEntity> findByTenantIdAndAvailable(UUID tenantId, boolean available);

    List<ProductJpaEntity> findByTenantIdAndAvailableOrderByCreatedAtDesc(UUID tenantId, boolean available);

    List<ProductJpaEntity> findByCategoryIdAndAvailable(UUID categoryId, boolean available);

    List<ProductJpaEntity> findByCategoryIdInAndAvailable(List<UUID> categoryIds, boolean available);

    List<ProductJpaEntity> findBySubcategoryIdAndAvailable(UUID subcategoryId, boolean available);

    Optional<ProductJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<ProductJpaEntity> findByIdAndTenantIdAndAvailable(UUID id, UUID tenantId, boolean available);

    List<ProductJpaEntity> findByTenantIdAndNameContainingIgnoreCaseAndAvailable(UUID tenantId, String keyword, boolean available);

    List<ProductJpaEntity> findByTenantId(UUID tenantId);
}
