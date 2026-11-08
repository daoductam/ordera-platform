package com.ordera.modules.reward.infrastructure.persistence.repository;

import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVoucherJpaRepository extends JpaRepository<UserVoucherJpaEntity, UUID> {

    List<UserVoucherJpaEntity> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);

    List<UserVoucherJpaEntity> findByTenantIdAndMemberIdAndStatus(UUID tenantId, UUID memberId, String status);

    List<UserVoucherJpaEntity> findByMemberIdAndStatus(UUID memberId, String status);

    Optional<UserVoucherJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<UserVoucherJpaEntity> findByTenantIdAndMemberIdAndVoucherIdAndStatus(UUID tenantId, UUID memberId, UUID voucherId, String status);

    Optional<UserVoucherJpaEntity> findByTenantIdAndAppliedOrderId(UUID tenantId, UUID appliedOrderId);

    Optional<UserVoucherJpaEntity> findByAppliedOrderId(UUID appliedOrderId);

    long countByTenantIdAndMemberIdAndStatus(UUID tenantId, UUID memberId, String status);
}

