package com.ordera.modules.reward.infrastructure.persistence.repository;

import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserGiftRedemptionJpaRepository extends JpaRepository<UserGiftRedemptionJpaEntity, UUID> {

    List<UserGiftRedemptionJpaEntity> findByTenantIdAndMemberIdOrderByCreatedAtDesc(UUID tenantId, UUID memberId);

    List<UserGiftRedemptionJpaEntity> findByTenantIdAndMemberIdAndStatus(UUID tenantId, UUID memberId, String status);

    List<UserGiftRedemptionJpaEntity> findByMemberIdAndStatus(UUID memberId, String status);

    Optional<UserGiftRedemptionJpaEntity> findByRedemptionCode(String redemptionCode);

    Optional<UserGiftRedemptionJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<UserGiftRedemptionJpaEntity> findByTenantIdAndAppliedOrderId(UUID tenantId, UUID appliedOrderId);

    long countByTenantIdAndMemberIdAndStatus(UUID tenantId, UUID memberId, String status);
}

