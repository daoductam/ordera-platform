package com.ordera.modules.reward.infrastructure.persistence.repository;

import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucherJpaEntity, UUID> {

    List<UserVoucherJpaEntity> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);

    List<UserVoucherJpaEntity> findByMemberIdAndStatus(UUID memberId, String status);

    Optional<UserVoucherJpaEntity> findByAppliedOrderId(UUID orderId);
}
