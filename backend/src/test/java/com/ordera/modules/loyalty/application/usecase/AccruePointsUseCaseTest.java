package com.ordera.modules.loyalty.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.ledger.domain.model.TransactionType;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import com.ordera.modules.ledger.infrastructure.persistence.repository.PointsLedgerJpaRepository;
import com.ordera.modules.loyalty.application.usecase.impl.AccruePointsUseCaseImpl;
import com.ordera.modules.loyalty.domain.exception.LoyaltyErrorCode;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccruePointsUseCaseTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @Mock
    private MemberProfileJpaRepository memberProfileRepository;

    @Mock
    private TierDefinitionJpaRepository tierDefinitionRepository;

    @Mock
    private PointsLedgerJpaRepository pointsLedgerRepository;

    @InjectMocks
    private AccruePointsUseCaseImpl accruePointsUseCase;

    private UUID tenantId;
    private UUID memberId;
    private UUID newTierId;
    private UUID silverTierId;
    private UUID goldTierId;
    private UUID rubyTierId;

    private MemberProfileJpaEntity memberProfile;
    private List<TierDefinitionJpaEntity> tiersDesc;

    @BeforeEach
    void setUp() throws InterruptedException {
        tenantId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        newTierId = UUID.randomUUID();
        silverTierId = UUID.randomUUID();
        goldTierId = UUID.randomUUID();
        rubyTierId = UUID.randomUUID();

        memberProfile = MemberProfileJpaEntity.builder()
                .userId(UUID.randomUUID())
                .currentTierId(newTierId)
                .currentPoints(50)
                .lifetimePoints(50)
                .build();
        memberProfile.setId(memberId);
        memberProfile.setTenantId(tenantId);

        TierDefinitionJpaEntity ruby = TierDefinitionJpaEntity.builder().tierName("Ruby").tierOrder(6).minPointsRequired(4000).build();
        ruby.setId(rubyTierId);
        TierDefinitionJpaEntity gold = TierDefinitionJpaEntity.builder().tierName("Gold").tierOrder(3).minPointsRequired(400).build();
        gold.setId(goldTierId);
        TierDefinitionJpaEntity silver = TierDefinitionJpaEntity.builder().tierName("Silver").tierOrder(2).minPointsRequired(200).build();
        silver.setId(silverTierId);
        TierDefinitionJpaEntity newTier = TierDefinitionJpaEntity.builder().tierName("New").tierOrder(1).minPointsRequired(0).build();
        newTier.setId(newTierId);

        tiersDesc = List.of(ruby, gold, silver, newTier);

        lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
        lenient().when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        lenient().when(rLock.isHeldByCurrentThread()).thenReturn(true);
        lenient().when(pointsLedgerRepository.save(any(PointsLedgerJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("should accrue points successfully, insert immutable ledger entry and acquire/release Redisson lock")
    void should_accrue_points_successfully() {
        // Arrange
        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(TransactionType.ORDER_EARN)
                .pointsDelta(100)
                .referenceType("ORDERS")
                .referenceId("ORD-001")
                .description("Tích điểm đơn hàng #001")
                .build();

        when(memberProfileRepository.findByIdAndTenantId(memberId, tenantId)).thenReturn(Optional.of(memberProfile));
        when(tierDefinitionRepository.findByTenantIdOrderByTierOrderDesc(tenantId)).thenReturn(tiersDesc);

        // Act
        PointsLedgerJpaEntity result = accruePointsUseCase.execute(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPointsDelta()).isEqualTo(100);
        assertThat(result.getBalanceAfter()).isEqualTo(150);
        assertThat(result.getTransactionType()).isEqualTo("ORDER_EARN");
        assertThat(result.getReferenceId()).isEqualTo("ORD-001");

        // Verify Redisson lock
        String expectedLockKey = "lock:tenant:" + tenantId + ":member:" + memberId;
        verify(redissonClient).getLock(expectedLockKey);
        verify(rLock).unlock();

        // Verify ledger immutability (only save is called, never delete)
        verify(pointsLedgerRepository, times(1)).save(any(PointsLedgerJpaEntity.class));
        verify(pointsLedgerRepository, never()).delete(any());

        // Verify profile updated
        assertThat(memberProfile.getCurrentPoints()).isEqualTo(150);
        assertThat(memberProfile.getLifetimePoints()).isEqualTo(150);
    }

    @Test
    @DisplayName("should automatically upgrade tier when lifetime points reach threshold (New -> Silver)")
    void should_auto_upgrade_tier_when_threshold_reached() {
        // Arrange
        memberProfile.setLifetimePoints(180);
        memberProfile.setCurrentPoints(180);

        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(TransactionType.ORDER_EARN)
                .pointsDelta(30) // 180 + 30 = 210 >= 200 (Silver)
                .referenceType("ORDERS")
                .referenceId("ORD-002")
                .description("Tích điểm đơn hàng #002")
                .build();

        when(memberProfileRepository.findByIdAndTenantId(memberId, tenantId)).thenReturn(Optional.of(memberProfile));
        when(tierDefinitionRepository.findByTenantIdOrderByTierOrderDesc(tenantId)).thenReturn(tiersDesc);

        // Act
        accruePointsUseCase.execute(command);

        // Assert: Tier upgraded to Silver
        assertThat(memberProfile.getCurrentTierId()).isEqualTo(silverTierId);
        assertThat(memberProfile.getLifetimePoints()).isEqualTo(210);
        assertThat(memberProfile.getCurrentPoints()).isEqualTo(210);
        verify(memberProfileRepository).save(memberProfile);
    }

    @Test
    @DisplayName("should automatically upgrade tier to Ruby when lifetime points reach 4000")
    void should_auto_upgrade_tier_to_ruby() {
        // Arrange
        memberProfile.setLifetimePoints(3950);
        memberProfile.setCurrentPoints(1000);
        memberProfile.setCurrentTierId(goldTierId);

        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(TransactionType.ORDER_EARN)
                .pointsDelta(60) // 3950 + 60 = 4010 >= 4000 (Ruby)
                .referenceType("ORDERS")
                .referenceId("ORD-003")
                .description("Tích điểm đơn hàng #003")
                .build();

        when(memberProfileRepository.findByIdAndTenantId(memberId, tenantId)).thenReturn(Optional.of(memberProfile));
        when(tierDefinitionRepository.findByTenantIdOrderByTierOrderDesc(tenantId)).thenReturn(tiersDesc);

        // Act
        accruePointsUseCase.execute(command);

        // Assert
        assertThat(memberProfile.getCurrentTierId()).isEqualTo(rubyTierId);
        assertThat(memberProfile.getLifetimePoints()).isEqualTo(4010);
        assertThat(memberProfile.getCurrentPoints()).isEqualTo(1060);
    }

    @Test
    @DisplayName("should deduct points on voucher redeem without decreasing lifetime points")
    void should_deduct_points_on_voucher_redeem() {
        // Arrange
        memberProfile.setCurrentPoints(300);
        memberProfile.setLifetimePoints(500);

        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(TransactionType.VOUCHER_REDEEM)
                .pointsDelta(-100)
                .referenceType("USER_VOUCHERS")
                .referenceId("UV-999")
                .description("Đổi voucher 100 NOK")
                .build();

        when(memberProfileRepository.findByIdAndTenantId(memberId, tenantId)).thenReturn(Optional.of(memberProfile));
        when(tierDefinitionRepository.findByTenantIdOrderByTierOrderDesc(tenantId)).thenReturn(tiersDesc);

        // Act
        PointsLedgerJpaEntity result = accruePointsUseCase.execute(command);

        // Assert
        assertThat(result.getPointsDelta()).isEqualTo(-100);
        assertThat(result.getBalanceAfter()).isEqualTo(200);
        assertThat(memberProfile.getCurrentPoints()).isEqualTo(200);
        assertThat(memberProfile.getLifetimePoints()).isEqualTo(500); // Unchanged!
    }

    @Test
    @DisplayName("should throw INSUFFICIENT_POINTS when deducting more points than current balance")
    void should_throw_insufficient_points_when_balance_not_enough() {
        // Arrange
        memberProfile.setCurrentPoints(40);

        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(TransactionType.VOUCHER_REDEEM)
                .pointsDelta(-100)
                .build();

        when(memberProfileRepository.findByIdAndTenantId(memberId, tenantId)).thenReturn(Optional.of(memberProfile));

        // Act & Assert
        assertThatThrownBy(() -> accruePointsUseCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == LoyaltyErrorCode.INSUFFICIENT_POINTS);

        verify(pointsLedgerRepository, never()).save(any());
        verify(rLock).unlock(); // Always unlocks in finally
    }

    @Test
    @DisplayName("should throw CONCURRENT_TRANSACTION_LOCK when Redisson lock cannot be acquired")
    void should_throw_concurrent_lock_when_lock_acquisition_fails() throws InterruptedException {
        // Arrange
        AccruePointsCommand command = AccruePointsCommand.builder()
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType(TransactionType.ORDER_EARN)
                .pointsDelta(50)
                .build();

        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> accruePointsUseCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .matches(ex -> ((BusinessException) ex).getErrorCode() == LoyaltyErrorCode.CONCURRENT_TRANSACTION_LOCK);

        verify(memberProfileRepository, never()).findByIdAndTenantId(any(), any());
    }
}

