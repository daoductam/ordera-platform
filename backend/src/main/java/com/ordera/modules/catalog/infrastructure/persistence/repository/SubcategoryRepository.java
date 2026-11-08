package com.ordera.modules.catalog.infrastructure.persistence.repository;

import com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubcategoryRepository extends JpaRepository<SubcategoryJpaEntity, UUID> {

    List<SubcategoryJpaEntity> findByCategoryIdAndActiveOrderByDisplayOrderAsc(UUID categoryId, boolean active);

    List<SubcategoryJpaEntity> findByCategoryIdInAndActiveOrderByDisplayOrderAsc(List<UUID> categoryIds, boolean active);

    List<SubcategoryJpaEntity> findByCategoryIdOrderByDisplayOrderAsc(UUID categoryId);
}
