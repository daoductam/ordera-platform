package com.ordera.modules.auth.infrastructure.persistence.repository;

import com.ordera.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<UserJpaEntity> findByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);

    Optional<UserJpaEntity> findByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndEmailAndIdNot(UUID tenantId, String email, UUID id);

    List<UserJpaEntity> findByTenantIdAndRole(UUID tenantId, String role);
}
