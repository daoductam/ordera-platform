package com.ordera.modules.reward.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.ledger.domain.model.TransactionType;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import com.ordera.modules.loyalty.application.usecase.AccruePointsCommand;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.usecase.RedeemVoucherUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.RedeemVoucherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Đổi điểm tích lũy lấy mã giảm giá Voucher (Figma 07.04).
 * Chiếm giữ Redisson Lock chống race condition bấm đúp.
 * Ghi sổ cái bất biến qua AccruePointsUseCase (VOUCHER_REDEEM).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedeemVoucherUseCaseImpl implements RedeemVoucherUseCase {

    private final RedissonClient redissonClient;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final VoucherJpaRepository voucherRepository;
    private final UserVoucherJpaRepository userVoucherRepository;
    private final AccruePointsUseCase accruePointsUseCase;

    @Autowired(required = false)
    private PlatformTransactionManager transactionManager;

    @Override
    public RedeemVoucherResponse execute(UUID voucherId) {
        Objects.requireNonNull(voucherId, "voucherId must not be null");
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity memberProfile = resolveMemberProfile(tenantId, userId);

        String lockKey = String.format(RewardConstants.LOCK_KEY_MEMBER_PATTERN, tenantId, memberProfile.getId());
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = tryAcquireLock(lock, lockKey);
        if (!acquired) {
            throw new BusinessException(RewardErrorCode.CONCURRENT_TRANSACTION_LOCK);
        }

        try {
            if (transactionManager != null) {
                TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
                return txTemplate.execute(status -> processRedemption(tenantId, memberProfile, voucherId));
            }
            return processRedemption(tenantId, memberProfile, voucherId);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean tryAcquireLock(RLock lock, String lockKey) {
        try {
            return lock.tryLock(
                    RewardConstants.LOCK_ACQUIRE_TIMEOUT_SEC,
                    RewardConstants.LOCK_LEASE_TIMEOUT_SEC,
                    TimeUnit.SECONDS
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while trying to acquire lock for key: {}", lockKey);
            throw new BusinessException(RewardErrorCode.CONCURRENT_TRANSACTION_LOCK);
        }
    }

    private RedeemVoucherResponse processRedemption(UUID tenantId, MemberProfileJpaEntity memberProfile, UUID voucherId) {
        VoucherJpaEntity voucher = voucherRepository.findByIdAndTenantId(voucherId, tenantId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.VOUCHER_NOT_FOUND));

        validateVoucherEligibility(voucher);
        validatePointsSufficiency(memberProfile, voucher);

        // 1. Trừ điểm khả dụng qua AccruePointsUseCase
        PointsLedgerJpaEntity ledgerEntry = deductPointsViaLedger(tenantId, memberProfile.getId(), voucher);

        // 2. Thêm bản ghi vào ví user_vouchers
        UserVoucherJpaEntity userVoucher = saveUserVoucherRecord(tenantId, memberProfile.getId(), voucher.getId());

        log.info("Member {} successfully redeemed voucher {} for {} points",
                memberProfile.getId(), voucher.getCode(), voucher.getPointsCost());

        return RedeemVoucherResponse.builder()
                .userVoucherId(userVoucher.getId())
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .pointsSpent(voucher.getPointsCost())
                .remainingPoints(ledgerEntry.getBalanceAfter())
                .validUntil(voucher.getValidUntil())
                .status(userVoucher.getStatus())
                .build();
    }

    private void validateVoucherEligibility(VoucherJpaEntity voucher) {
        if (!voucher.isActive()) {
            throw new BusinessException(RewardErrorCode.VOUCHER_NOT_FOUND);
        }
        if (voucher.getValidUntil() != null && voucher.getValidUntil().isBefore(Instant.now())) {
            throw new BusinessException(RewardErrorCode.VOUCHER_EXPIRED);
        }
    }

    private void validatePointsSufficiency(MemberProfileJpaEntity profile, VoucherJpaEntity voucher) {
        int cost = voucher.getPointsCost();
        int currentPoints = profile.getCurrentPoints();
        if (currentPoints < cost) {
            Map<String, Object> details = Map.of(
                    "currentPoints", currentPoints,
                    "requiredPoints", cost,
                    "pointsShortage", cost - currentPoints
            );
            throw new BusinessException(RewardErrorCode.INSUFFICIENT_POINTS,
                    RewardErrorCode.INSUFFICIENT_POINTS.getDefaultMessage(), details);
        }
    }

    private PointsLedgerJpaEntity deductPointsViaLedger(UUID tenantId, UUID memberId, VoucherJpaEntity voucher) {
        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(TransactionType.VOUCHER_REDEEM)
                .pointsDelta(-voucher.getPointsCost())
                .referenceType("VOUCHER")
                .referenceId(voucher.getId().toString())
                .description("Đổi voucher " + voucher.getTitle())
                .build();
        return accruePointsUseCase.execute(command);
    }

    private UserVoucherJpaEntity saveUserVoucherRecord(UUID tenantId, UUID memberId, UUID voucherId) {
        UserVoucherJpaEntity entity = UserVoucherJpaEntity.builder()
                .memberId(memberId)
                .voucherId(voucherId)
                .status(RewardConstants.STATUS_AVAILABLE)
                .redeemedAt(Instant.now())
                .build();
        entity.setTenantId(tenantId);
        return userVoucherRepository.save(entity);
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(RewardErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private UUID resolveUserId() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(RewardErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    private MemberProfileJpaEntity resolveMemberProfile(UUID tenantId, UUID userId) {
        return memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.MEMBER_PROFILE_NOT_FOUND));
    }
}
