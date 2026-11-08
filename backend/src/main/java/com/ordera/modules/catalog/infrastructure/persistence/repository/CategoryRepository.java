package com.ordera.modules.catalog.infrastructure.persistence.repository;

import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    List<CategoryJpaEntity> findByTenantIdAndActiveOrderByDisplayOrderAsc(UUID tenantId, boolean active);

    List<CategoryJpaEntity> findByTenantIdOrderByDisplayOrderAsc(UUID tenantId);

    Optional<CategoryJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
