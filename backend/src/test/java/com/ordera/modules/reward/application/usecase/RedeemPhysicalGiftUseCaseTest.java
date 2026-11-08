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
class RedeemPhysicalGiftUseCaseTest {

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock memberLock;
    @Mock
    private RLock giftLock;
    @Mock
    private RLock multiLock;
    @Mock
    private MemberProfileJpaRepository memberProfileRepository;
    @Mock
    private LoyaltyGiftJpaRepository loyaltyGiftRepository;
    @Mock
    private UserGiftRedemptionJpaRepository userGiftRedemptionRepository;
    @Mock
    private AccruePointsUseCase accruePointsUseCase;

    private RedeemPhysicalGiftUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID giftId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws InterruptedException {
        TenantContext.setTenantId(tenantId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );

        lenient().when(redissonClient.getLock(contains("member"))).thenReturn(memberLock);
        lenient().when(redissonClient.getLock(contains("gift"))).thenReturn(giftLock);
        lenient().when(redissonClient.getMultiLock(any(RLock.class), any(RLock.class))).thenReturn(multiLock);
        lenient().when(multiLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        lenient().when(multiLock.isHeldByCurrentThread()).thenReturn(true);

        useCase = new RedeemPhysicalGiftUseCaseImpl(
                redissonClient,
                memberProfileRepository,
                loyaltyGiftRepository,
                userGiftRedemptionRepository,
                accruePointsUseCase
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should successfully redeem physical gift, decrement inventory, and generate 8-char code")
    void should_successfully_redeem_gift_when_stock_and_points_are_sufficient() {
        // Arrange
        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentPoints(1500)
                .build();
        profile.setId(memberId);
        profile.setTenantId(tenantId);

        LoyaltyGiftJpaEntity gift = LoyaltyGiftJpaEntity.builder()
                .title("Tumbler Pink")
                .pointsCost(1000)
                .stockQuantity(10)
                .active(true)
                .build();
        gift.setId(giftId);
        gift.setTenantId(tenantId);

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
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

        // Act
        RedeemGiftResponse response = useCase.execute(giftId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getGiftTitle()).isEqualTo("Tumbler Pink");
        assertThat(response.getPointsSpent()).isEqualTo(1000);
        assertThat(response.getRemainingPoints()).isEqualTo(500);
        assertThat(response.getRedemptionCode()).hasSize(8);
        assertThat(response.getStatus()).isEqualTo(RewardConstants.STATUS_PENDING);

        assertThat(gift.getStockQuantity()).isEqualTo(9);
        verify(loyaltyGiftRepository).save(gift);
        verify(accruePointsUseCase).execute(any(AccruePointsCommand.class));
        verify(userGiftRedemptionRepository).save(any(UserGiftRedemptionJpaEntity.class));
        verify(multiLock).unlock();
    }

    @Test
    @DisplayName("should throw GIFT_OUT_OF_STOCK when gift stockQuantity is 0")
    void should_throw_GIFT_OUT_OF_STOCK_when_stock_is_zero() {
        // Arrange
        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentPoints(2000)
                .build();
        profile.setId(memberId);

        LoyaltyGiftJpaEntity gift = LoyaltyGiftJpaEntity.builder()
                .title("Tumbler Pink")
                .pointsCost(1000)
                .stockQuantity(0)
                .active(true)
                .build();
        gift.setId(giftId);

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(loyaltyGiftRepository.findByIdAndTenantId(giftId, tenantId)).thenReturn(Optional.of(gift));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(giftId))
                .isInstanceOf(BusinessException.class)
                .matches(e -> ((BusinessException) e).getErrorCode() == RewardErrorCode.GIFT_OUT_OF_STOCK);

        verify(accruePointsUseCase, never()).execute(any());
        verify(multiLock).unlock();
    }

    @Test
    @DisplayName("should throw INSUFFICIENT_POINTS when member does not have enough points for gift")
    void should_throw_INSUFFICIENT_POINTS_when_member_points_insufficient() {
        // Arrange
        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentPoints(500)
                .build();
        profile.setId(memberId);

        LoyaltyGiftJpaEntity gift = LoyaltyGiftJpaEntity.builder()
                .title("Tumbler Pink")
                .pointsCost(1000)
                .stockQuantity(5)
                .active(true)
                .build();
        gift.setId(giftId);

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(loyaltyGiftRepository.findByIdAndTenantId(giftId, tenantId)).thenReturn(Optional.of(gift));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(giftId))
                .isInstanceOf(BusinessException.class)
                .matches(e -> ((BusinessException) e).getErrorCode() == RewardErrorCode.INSUFFICIENT_POINTS);

        verify(accruePointsUseCase, never()).execute(any());
        verify(multiLock).unlock();
    }

    @Test
    @DisplayName("should throw CONCURRENT_TRANSACTION_LOCK when multiLock cannot be acquired")
    void should_throw_CONCURRENT_TRANSACTION_LOCK_when_multi_lock_fails() throws InterruptedException {
        // Arrange
        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder().userId(userId).build();
        profile.setId(memberId);
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(multiLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(giftId))
                .isInstanceOf(BusinessException.class)
                .matches(e -> ((BusinessException) e).getErrorCode() == RewardErrorCode.CONCURRENT_TRANSACTION_LOCK);

        verify(loyaltyGiftRepository, never()).findByIdAndTenantId(any(), any());
    }
}

