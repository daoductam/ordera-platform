package com.ordera.modules.loyalty.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import com.ordera.modules.ledger.infrastructure.persistence.repository.PointsLedgerJpaRepository;
import com.ordera.modules.loyalty.application.usecase.AccruePointsCommand;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccruePointsUseCaseImpl implements AccruePointsUseCase {

    private final RedissonClient redissonClient;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final TierDefinitionJpaRepository tierDefinitionRepository;
    private final PointsLedgerJpaRepository pointsLedgerRepository;

    @Override
    @Transactional
    public PointsLedgerJpaEntity execute(AccruePointsCommand command) {
        Objects.requireNonNull(command, "AccruePointsCommand must not be null");
        UUID tenantId = command.getTenantId();
        UUID memberId = command.getMemberId();

        String lockKey = "lock:tenant:" + tenantId + ":member:" + memberId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired;
        try {
            acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(LoyaltyErrorCode.CONCURRENT_TRANSACTION_LOCK, "Luồng xử lý bị gián đoạn khi chờ khóa điểm.");
        }

        if (!acquired) {
            log.warn("Failed to acquire Redisson lock for key: {}", lockKey);
            throw new BusinessException(LoyaltyErrorCode.CONCURRENT_TRANSACTION_LOCK);
        }

        try {
            return processAccrual(command);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private PointsLedgerJpaEntity processAccrual(AccruePointsCommand command) {
        UUID tenantId = command.getTenantId();
        UUID memberId = command.getMemberId();

        MemberProfileJpaEntity memberProfile = memberProfileRepository.findByIdAndTenantId(memberId, tenantId)
                .orElseThrow(() -> new BusinessException(LoyaltyErrorCode.MEMBER_PROFILE_NOT_FOUND));

        int delta = command.getPointsDelta();
        int currentPoints = memberProfile.getCurrentPoints();

        // 1. Kiểm tra số dư nếu là giao dịch trừ điểm
        if (delta < 0 && (currentPoints + delta < 0)) {
            throw new BusinessException(LoyaltyErrorCode.INSUFFICIENT_POINTS);
        }

        int newCurrentPoints = currentPoints + delta;
        int newLifetimePoints = memberProfile.getLifetimePoints() + (delta > 0 ? delta : 0);

        // 2. Tự động xét mốc thăng hạng thẻ
        UUID newTierId = evaluateTierUpgrade(tenantId, newLifetimePoints, memberProfile.getCurrentTierId());
        if (!newTierId.equals(memberProfile.getCurrentTierId())) {
            log.info("Member {} upgraded to new tier: {} (lifetimePoints: {})", memberId, newTierId, newLifetimePoints);
            memberProfile.setCurrentTierId(newTierId);
        }

        // 3. Ghi nhận sổ cái kép bất biến (POSTGRESQL RANGE PARTITIONED - CẤM SỬA/XÓA)
        PointsLedgerJpaEntity ledgerEntry = PointsLedgerJpaEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(command.getTransactionType().name())
                .pointsDelta(delta)
                .balanceAfter(newCurrentPoints)
                .referenceType(command.getReferenceType())
                .referenceId(command.getReferenceId())
                .description(command.getDescription() != null ? command.getDescription() : command.getTransactionType().name())
                .build();

        PointsLedgerJpaEntity savedLedger = pointsLedgerRepository.save(ledgerEntry);

        // 4. Cập nhật cache số dư trên member_profiles
        memberProfile.setCurrentPoints(newCurrentPoints);
        memberProfile.setLifetimePoints(newLifetimePoints);
        memberProfile.setUpdatedAt(Instant.now());
        memberProfileRepository.save(memberProfile);

        return savedLedger;
    }

    private UUID evaluateTierUpgrade(UUID tenantId, int lifetimePoints, UUID currentTierId) {
        List<TierDefinitionJpaEntity> tiers = tierDefinitionRepository.findByTenantIdOrderByTierOrderDesc(tenantId);
        if (tiers == null || tiers.isEmpty()) {
            return currentTierId;
        }

        for (TierDefinitionJpaEntity tier : tiers) {
            if (lifetimePoints >= tier.getMinPointsRequired()) {
                return tier.getId();
            }
        }
        return currentTierId;
    }
}

