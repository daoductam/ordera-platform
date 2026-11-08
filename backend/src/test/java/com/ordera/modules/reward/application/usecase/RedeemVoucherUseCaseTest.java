package com.ordera.modules.reward.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import com.ordera.modules.loyalty.application.usecase.AccruePointsCommand;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.usecase.impl.RedeemVoucherUseCaseImpl;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.RedeemVoucherResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedeemVoucherUseCaseTest {

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock lock;
    @Mock
    private MemberProfileJpaRepository memberProfileRepository;
    @Mock
    private VoucherJpaRepository voucherRepository;
    @Mock
    private UserVoucherJpaRepository userVoucherRepository;
    @Mock
    private AccruePointsUseCase accruePointsUseCase;

    private RedeemVoucherUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID voucherId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws InterruptedException {
        TenantContext.setTenantId(tenantId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );

        lenient().when(redissonClient.getLock(anyString())).thenReturn(lock);
        lenient().when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        lenient().when(lock.isHeldByCurrentThread()).thenReturn(true);

        useCase = new RedeemVoucherUseCaseImpl(
                redissonClient,
                memberProfileRepository,
                voucherRepository,
                userVoucherRepository,
                accruePointsUseCase
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should successfully redeem voucher when points are sufficient and lock is acquired")
    void should_successfully_redeem_voucher_when_points_are_sufficient() {
        // Arrange
        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentPoints(200)
                .build();
        profile.setId(memberId);
        profile.setTenantId(tenantId);

        VoucherJpaEntity voucher = VoucherJpaEntity.builder()
                .code("SAVE100")
                .title("Voucher 100 NOK")
                .pointsCost(100)
                .discountType(RewardConstants.DISCOUNT_TYPE_FIXED)
                .discountValue(BigDecimal.valueOf(100))
                .minOrderValue(BigDecimal.valueOf(250))
                .validUntil(Instant.now().plusSeconds(3600))
                .active(true)
                .build();
        voucher.setId(voucherId);
        voucher.setTenantId(tenantId);

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(voucherRepository.findByIdAndTenantId(voucherId, tenantId)).thenReturn(Optional.of(voucher));
        when(accruePointsUseCase.execute(any(AccruePointsCommand.class))).thenReturn(
                PointsLedgerJpaEntity.builder().balanceAfter(100).build()
        );
        when(userVoucherRepository.save(any(UserVoucherJpaEntity.class))).thenAnswer(inv -> {
            UserVoucherJpaEntity e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        // Act
        RedeemVoucherResponse response = useCase.execute(voucherId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("SAVE100");
        assertThat(response.getPointsSpent()).isEqualTo(100);
        assertThat(response.getRemainingPoints()).isEqualTo(100);
        assertThat(response.getStatus()).isEqualTo(RewardConstants.STATUS_AVAILABLE);

        verify(accruePointsUseCase).execute(any(AccruePointsCommand.class));
        verify(userVoucherRepository).save(any(UserVoucherJpaEntity.class));
        verify(lock).unlock();
    }

    @Test
    @DisplayName("should throw INSUFFICIENT_POINTS when current points are less than cost")
    void should_throw_INSUFFICIENT_POINTS_when_points_are_less_than_cost() {
        // Arrange
        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentPoints(50)
                .build();
        profile.setId(memberId);

        VoucherJpaEntity voucher = VoucherJpaEntity.builder()
                .pointsCost(100)
                .validUntil(Instant.now().plusSeconds(3600))
                .active(true)
                .build();
        voucher.setId(voucherId);

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(voucherRepository.findByIdAndTenantId(voucherId, tenantId)).thenReturn(Optional.of(voucher));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(voucherId))
                .isInstanceOf(BusinessException.class)
                .matches(e -> ((BusinessException) e).getErrorCode() == RewardErrorCode.INSUFFICIENT_POINTS);

        verify(accruePointsUseCase, never()).execute(any());
        verify(lock).unlock();
    }

    @Test
    @DisplayName("should throw CONCURRENT_TRANSACTION_LOCK when Redisson lock cannot be acquired")
    void should_throw_CONCURRENT_TRANSACTION_LOCK_when_lock_cannot_be_acquired() throws InterruptedException {
        // Arrange
        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder().userId(userId).build();
        profile.setId(memberId);
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(voucherId))
                .isInstanceOf(BusinessException.class)
                .matches(e -> ((BusinessException) e).getErrorCode() == RewardErrorCode.CONCURRENT_TRANSACTION_LOCK);

        verify(voucherRepository, never()).findByIdAndTenantId(any(), any());
    }

    @Test
    @DisplayName("should throw VOUCHER_EXPIRED when voucher validUntil is in the past")
    void should_throw_VOUCHER_EXPIRED_when_validUntil_is_past() {
        // Arrange
        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentPoints(500)
                .build();
        profile.setId(memberId);

        VoucherJpaEntity voucher = VoucherJpaEntity.builder()
                .pointsCost(100)
                .validUntil(Instant.now().minusSeconds(3600))
                .active(true)
                .build();
        voucher.setId(voucherId);

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(voucherRepository.findByIdAndTenantId(voucherId, tenantId)).thenReturn(Optional.of(voucher));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(voucherId))
                .isInstanceOf(BusinessException.class)
                .matches(e -> ((BusinessException) e).getErrorCode() == RewardErrorCode.VOUCHER_EXPIRED);

        verify(lock).unlock();
    }
}
