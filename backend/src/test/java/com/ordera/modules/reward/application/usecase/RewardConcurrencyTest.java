package com.ordera.modules.reward.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import com.ordera.modules.loyalty.application.usecase.AccruePointsCommand;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.usecase.impl.RedeemPhysicalGiftUseCaseImpl;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserGiftRedemptionJpaRepository;
import com.ordera.modules.reward.presentation.dto.RedeemGiftResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardConcurrencyTest {

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock memberLock;
    @Mock
    private RLock giftLock;
    @Mock
    private MemberProfileJpaRepository memberProfileRepository;
    @Mock
    private LoyaltyGiftJpaRepository loyaltyGiftRepository;
    @Mock
    private UserGiftRedemptionJpaRepository userGiftRedemptionRepository;
    @Mock
    private AccruePointsUseCase accruePointsUseCase;

    @Test
    @DisplayName("should allow only 1 thread to redeem when 2 concurrent threads compete for the last gift (stock=1)")
    void should_allow_only_one_thread_to_redeem_last_gift() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID memberId1 = UUID.randomUUID();
        UUID memberId2 = UUID.randomUUID();
        UUID giftId = UUID.randomUUID();

        // Realistic distributed lock simulation using Java ReentrantLock
        ReentrantLock realLock = new ReentrantLock();
        RLock multiLockMock = mock(RLock.class);

        when(redissonClient.getLock(anyString())).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            return key.contains("member") ? memberLock : giftLock;
        });
        when(redissonClient.getMultiLock(any(RLock.class), any(RLock.class))).thenReturn(multiLockMock);

        when(multiLockMock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenAnswer(inv -> {
            long wait = inv.getArgument(0);
            TimeUnit unit = inv.getArgument(2);
            return realLock.tryLock(wait, unit);
        });
        when(multiLockMock.isHeldByCurrentThread()).thenAnswer(inv -> realLock.isHeldByCurrentThread());
        doAnswer(inv -> {
            if (realLock.isHeldByCurrentThread()) {
                realLock.unlock();
            }
            return null;
        }).when(multiLockMock).unlock();

        // Member 1 setup
        MemberProfileJpaEntity profile1 = MemberProfileJpaEntity.builder().userId(userId1).currentPoints(1500).build();
        profile1.setId(memberId1);
        lenient().when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId1)).thenReturn(Optional.of(profile1));

        // Member 2 setup
        MemberProfileJpaEntity profile2 = MemberProfileJpaEntity.builder().userId(userId2).currentPoints(1500).build();
        profile2.setId(memberId2);
        lenient().when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId2)).thenReturn(Optional.of(profile2));

        // Shared gift with ONLY 1 item left
        LoyaltyGiftJpaEntity gift = LoyaltyGiftJpaEntity.builder()
                .title("Last Tumbler")
                .pointsCost(1000)
                .stockQuantity(1)
                .active(true)
                .build();
        gift.setId(giftId);
        gift.setTenantId(tenantId);
        when(loyaltyGiftRepository.findByIdAndTenantId(giftId, tenantId)).thenReturn(Optional.of(gift));

        when(userGiftRedemptionRepository.findByRedemptionCode(anyString())).thenReturn(Optional.empty());
        when(accruePointsUseCase.execute(any(AccruePointsCommand.class))).thenReturn(
                PointsLedgerJpaEntity.builder().balanceAfter(500).build()
        );
        when(userGiftRedemptionRepository.save(any(UserGiftRedemptionJpaEntity.class))).thenAnswer(inv -> {
            UserGiftRedemptionJpaEntity e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        RedeemPhysicalGiftUseCaseImpl useCase = new RedeemPhysicalGiftUseCaseImpl(
                redissonClient, memberProfileRepository, loyaltyGiftRepository, userGiftRedemptionRepository, accruePointsUseCase
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger outOfStockCount = new AtomicInteger(0);

        Runnable task1 = () -> {
            try {
                startSignal.await();
                TenantContext.setTenantId(tenantId);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId1.toString(), null)
                );
                RedeemGiftResponse res = useCase.execute(giftId);
                if (res != null) {
                    successCount.incrementAndGet();
                }
            } catch (BusinessException e) {
                if (e.getErrorCode() == RewardErrorCode.GIFT_OUT_OF_STOCK) {
                    outOfStockCount.incrementAndGet();
                }
            } catch (Exception ignored) {
            } finally {
                TenantContext.clear();
                SecurityContextHolder.clearContext();
                doneSignal.countDown();
            }
        };

        Runnable task2 = () -> {
            try {
                startSignal.await();
                TenantContext.setTenantId(tenantId);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId2.toString(), null)
                );
                RedeemGiftResponse res = useCase.execute(giftId);
                if (res != null) {
                    successCount.incrementAndGet();
                }
            } catch (BusinessException e) {
                if (e.getErrorCode() == RewardErrorCode.GIFT_OUT_OF_STOCK) {
                    outOfStockCount.incrementAndGet();
                }
            } catch (Exception ignored) {
            } finally {
                TenantContext.clear();
                SecurityContextHolder.clearContext();
                doneSignal.countDown();
            }
        };

        executor.submit(task1);
        executor.submit(task2);

        startSignal.countDown();
        doneSignal.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Exactly 1 thread must succeed and 1 must get out of stock!
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(outOfStockCount.get()).isEqualTo(1);
        assertThat(gift.getStockQuantity()).isEqualTo(0);
    }
}

