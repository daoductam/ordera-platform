package com.ordera.modules.reward.infrastructure.persistence.repository;

import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserGiftRedemptionRepository extends JpaRepository<UserGiftRedemptionJpaEntity, UUID> {

    List<UserGiftRedemptionJpaEntity> findByTenantIdAndMemberIdOrderByCreatedAtDesc(UUID tenantId, UUID memberId);

    Optional<UserGiftRedemptionJpaEntity> findByRedemptionCode(String redemptionCode);

    List<UserGiftRedemptionJpaEntity> findByMemberIdAndStatus(UUID memberId, String status);
}
