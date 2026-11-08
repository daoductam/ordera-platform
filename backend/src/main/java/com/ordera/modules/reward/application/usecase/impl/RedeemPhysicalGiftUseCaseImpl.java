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
import com.ordera.modules.reward.application.usecase.RedeemPhysicalGiftUseCase;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserGiftRedemptionJpaRepository;
import com.ordera.modules.reward.presentation.dto.RedeemGiftResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Đổi điểm nhận quà hiện vật (Cốc Tumbler) (Figma 07.04).
 * Chiếm giữ Redisson Multi-Lock đồng thời trên member và gift để chống race condition.
 * Trừ 1 tồn kho, sinh mã nhận quà 8 ký tự, trừ điểm qua AccruePointsUseCase (GIFT_REDEEM).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedeemPhysicalGiftUseCaseImpl implements RedeemPhysicalGiftUseCase {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedissonClient redissonClient;
    private final MemberProfileJpaRepository memberProfileRepository;
    private final LoyaltyGiftJpaRepository loyaltyGiftRepository;
    private final UserGiftRedemptionJpaRepository userGiftRedemptionRepository;
    private final AccruePointsUseCase accruePointsUseCase;

    @Autowired(required = false)
    private PlatformTransactionManager transactionManager;

    @Override
    public RedeemGiftResponse execute(UUID giftId) {
        Objects.requireNonNull(giftId, "giftId must not be null");
        UUID tenantId = resolveTenantId();
        UUID userId = resolveUserId();
        MemberProfileJpaEntity memberProfile = resolveMemberProfile(tenantId, userId);

        String memberLockKey = String.format(RewardConstants.LOCK_KEY_MEMBER_PATTERN, tenantId, memberProfile.getId());
        String giftLockKey = String.format(RewardConstants.LOCK_KEY_GIFT_PATTERN, tenantId, giftId);

        RLock memberLock = redissonClient.getLock(memberLockKey);
        RLock giftLock = redissonClient.getLock(giftLockKey);
        RLock multiLock = redissonClient.getMultiLock(memberLock, giftLock);

        boolean acquired = tryAcquireMultiLock(multiLock, memberLockKey, giftLockKey);
        if (!acquired) {
            throw new BusinessException(RewardErrorCode.CONCURRENT_TRANSACTION_LOCK);
        }

        try {
            if (transactionManager != null) {
                TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
                return txTemplate.execute(status -> processGiftRedemption(tenantId, memberProfile, giftId));
            }
            return processGiftRedemption(tenantId, memberProfile, giftId);
        } finally {
            if (multiLock.isHeldByCurrentThread()) {
                multiLock.unlock();
            }
        }
    }

    private boolean tryAcquireMultiLock(RLock multiLock, String memberKey, String giftKey) {
        try {
            return multiLock.tryLock(
                    RewardConstants.LOCK_ACQUIRE_TIMEOUT_SEC,
                    RewardConstants.LOCK_LEASE_TIMEOUT_SEC,
                    TimeUnit.SECONDS
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for multi-lock: {}, {}", memberKey, giftKey);
            throw new BusinessException(RewardErrorCode.CONCURRENT_TRANSACTION_LOCK);
        }
    }

    private RedeemGiftResponse processGiftRedemption(UUID tenantId, MemberProfileJpaEntity memberProfile, UUID giftId) {
        LoyaltyGiftJpaEntity gift = loyaltyGiftRepository.findByIdAndTenantId(giftId, tenantId)
                .orElseThrow(() -> new BusinessException(RewardErrorCode.GIFT_NOT_FOUND));

        validateGiftEligibility(gift);
        validatePointsSufficiency(memberProfile, gift);

        // 1. Trừ 1 tồn kho trong loyalty_gifts
        gift.setStockQuantity(gift.getStockQuantity() - 1);
        loyaltyGiftRepository.save(gift);

        // 2. Trừ điểm hội viên qua AccruePointsUseCase (GIFT_REDEEM)
        PointsLedgerJpaEntity ledgerEntry = deductPointsViaLedger(tenantId, memberProfile.getId(), gift);

        // 3. Sinh mã nhận quà 8 ký tự độc nhất
        String redemptionCode = generateUniqueRedemptionCode();

        // 4. Lưu bản ghi user_gift_redemptions
        UserGiftRedemptionJpaEntity redemption = saveRedemptionRecord(tenantId, memberProfile.getId(), gift, redemptionCode);

        log.info("Member {} redeemed gift {} ({}) successfully", memberProfile.getId(), gift.getTitle(), redemptionCode);

        return RedeemGiftResponse.builder()
                .redemptionId(redemption.getId())
                .giftTitle(gift.getTitle())
                .pointsSpent(gift.getPointsCost())
                .remainingPoints(ledgerEntry.getBalanceAfter())
                .redemptionCode(redemption.getRedemptionCode())
                .status(redemption.getStatus())
                .instruction("Xuất trình mã này tại quầy thu ngân bất kỳ chi nhánh để nhận quà.")
                .createdAt(redemption.getCreatedAt())
                .build();
    }

    private void validateGiftEligibility(LoyaltyGiftJpaEntity gift) {
        if (!gift.isActive()) {
            throw new BusinessException(RewardErrorCode.GIFT_NOT_FOUND);
        }
        if (gift.getStockQuantity() <= 0) {
            throw new BusinessException(RewardErrorCode.GIFT_OUT_OF_STOCK,
                    RewardErrorCode.GIFT_OUT_OF_STOCK.getDefaultMessage(),
                    Map.of("stockQuantity", 0));
        }
    }

    private void validatePointsSufficiency(MemberProfileJpaEntity profile, LoyaltyGiftJpaEntity gift) {
        int cost = gift.getPointsCost();
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

    private PointsLedgerJpaEntity deductPointsViaLedger(UUID tenantId, UUID memberId, LoyaltyGiftJpaEntity gift) {
        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(TransactionType.GIFT_REDEEM)
                .pointsDelta(-gift.getPointsCost())
                .referenceType("GIFT")
                .referenceId(gift.getId().toString())
                .description("Đổi quà " + gift.getTitle())
                .build();
        return accruePointsUseCase.execute(command);
    }

    private String generateUniqueRedemptionCode() {
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder(RewardConstants.GIFT_REDEMPTION_CODE_LENGTH);
            for (int j = 0; j < RewardConstants.GIFT_REDEMPTION_CODE_LENGTH; j++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (userGiftRedemptionRepository.findByRedemptionCode(code).isEmpty()) {
                return code;
            }
        }
        return UUID.randomUUID().toString().substring(0, RewardConstants.GIFT_REDEMPTION_CODE_LENGTH).toUpperCase();
    }

    private UserGiftRedemptionJpaEntity saveRedemptionRecord(UUID tenantId, UUID memberId, LoyaltyGiftJpaEntity gift, String code) {
        UserGiftRedemptionJpaEntity entity = UserGiftRedemptionJpaEntity.builder()
                .memberId(memberId)
                .giftId(gift.getId())
                .pointsSpent(gift.getPointsCost())
                .redemptionCode(code)
                .status(RewardConstants.STATUS_PENDING)
                .createdAt(Instant.now())
                .build();
        entity.setTenantId(tenantId);
        return userGiftRedemptionRepository.save(entity);
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
