package com.ordera.modules.tenant.infrastructure.persistence.repository;

import com.ordera.modules.tenant.infrastructure.persistence.entity.TenantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<TenantJpaEntity, UUID> {

    Optional<TenantJpaEntity> findByCode(String code);

    boolean existsByCode(String code);
}
