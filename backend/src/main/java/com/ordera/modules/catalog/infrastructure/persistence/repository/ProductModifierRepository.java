package com.ordera.modules.catalog.infrastructure.persistence.repository;

import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductModifierRepository extends JpaRepository<ProductModifierJpaEntity, UUID> {

    List<ProductModifierJpaEntity> findByGroupId(UUID groupId);

    List<ProductModifierJpaEntity> findByGroupIdIn(List<UUID> groupIds);
}
