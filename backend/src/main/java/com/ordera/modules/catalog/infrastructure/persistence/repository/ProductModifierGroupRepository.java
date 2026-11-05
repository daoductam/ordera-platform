package com.ordera.modules.catalog.infrastructure.persistence.repository;

import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductModifierGroupRepository extends JpaRepository<ProductModifierGroupJpaEntity, UUID> {

    List<ProductModifierGroupJpaEntity> findByProductIdOrderByDisplayOrderAsc(UUID productId);
}
