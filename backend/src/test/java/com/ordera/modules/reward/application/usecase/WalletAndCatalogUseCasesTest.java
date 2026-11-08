package com.ordera.modules.reward.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.usecase.impl.GetMyRewardsWalletUseCaseImpl;
import com.ordera.modules.reward.application.usecase.impl.GetRewardsCatalogUseCaseImpl;
import com.ordera.modules.reward.application.usecase.impl.GetRewardsSummaryUseCaseImpl;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.infrastructure.mapper.RewardDtoMapper;
import com.ordera.modules.reward.infrastructure.mapper.RewardDtoMapperImpl;
import com.ordera.modules.reward.infrastructure.persistence.entity.LoyaltyGiftJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserGiftRedemptionJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.UserVoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserGiftRedemptionJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import com.ordera.modules.reward.presentation.dto.MyRewardsWalletResponse;
import com.ordera.modules.reward.presentation.dto.RewardsCatalogResponse;
import com.ordera.modules.reward.presentation.dto.RewardsSummaryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletAndCatalogUseCasesTest {

    @Mock
    private MemberProfileJpaRepository memberProfileRepository;
    @Mock
    private UserVoucherJpaRepository userVoucherRepository;
    @Mock
    private VoucherJpaRepository voucherRepository;
    @Mock
    private UserGiftRedemptionJpaRepository userGiftRedemptionRepository;
    @Mock
    private LoyaltyGiftJpaRepository loyaltyGiftRepository;

    private final RewardDtoMapper rewardDtoMapper = new RewardDtoMapperImpl();

    private GetMyRewardsWalletUseCaseImpl walletUseCase;
    private GetRewardsCatalogUseCaseImpl catalogUseCase;
    private GetRewardsSummaryUseCaseImpl summaryUseCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );

        walletUseCase = new GetMyRewardsWalletUseCaseImpl(
                memberProfileRepository,
                userVoucherRepository,
                voucherRepository,
                userGiftRedemptionRepository,
                loyaltyGiftRepository,
                rewardDtoMapper
        );

        catalogUseCase = new GetRewardsCatalogUseCaseImpl(
                memberProfileRepository,
                voucherRepository,
                loyaltyGiftRepository,
                rewardDtoMapper
        );

        summaryUseCase = new GetRewardsSummaryUseCaseImpl(
                memberProfileRepository,
                userVoucherRepository,
                userGiftRedemptionRepository
        );

        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentPoints(150)
                .build();
        profile.setId(memberId);
        profile.setTenantId(tenantId);

        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should return wallet with both vouchers and physical gifts")
    void should_return_wallet_with_both_vouchers_and_physical_gifts() {
        UUID vId = UUID.randomUUID();
        UserVoucherJpaEntity uv = UserVoucherJpaEntity.builder()
                .memberId(memberId)
                .voucherId(vId)
                .status(RewardConstants.STATUS_AVAILABLE)
                .build();
        uv.setId(UUID.randomUUID());

        VoucherJpaEntity v = VoucherJpaEntity.builder()
                .code("SAVE100")
                .title("Voucher 100 NOK")
                .discountType(RewardConstants.DISCOUNT_TYPE_FIXED)
                .discountValue(BigDecimal.valueOf(100))
                .minOrderValue(BigDecimal.valueOf(250))
                .validUntil(Instant.now().plusSeconds(86400))
                .build();
        v.setId(vId);

        UUID gId = UUID.randomUUID();
        UserGiftRedemptionJpaEntity ug = UserGiftRedemptionJpaEntity.builder()
                .memberId(memberId)
                .giftId(gId)
                .redemptionCode("GIFT-8942")
                .status(RewardConstants.STATUS_PENDING)
                .build();
        ug.setId(UUID.randomUUID());

        LoyaltyGiftJpaEntity g = LoyaltyGiftJpaEntity.builder()
                .title("Tumbler Pink")
                .build();
        g.setId(gId);

        when(userVoucherRepository.findByTenantIdAndMemberIdAndStatus(tenantId, memberId, RewardConstants.STATUS_AVAILABLE))
                .thenReturn(List.of(uv));
        when(voucherRepository.findAllById(List.of(vId))).thenReturn(List.of(v));
        when(userGiftRedemptionRepository.findByTenantIdAndMemberIdAndStatus(tenantId, memberId, RewardConstants.STATUS_AVAILABLE))
                .thenReturn(List.of(ug));
        when(loyaltyGiftRepository.findAllById(List.of(gId))).thenReturn(List.of(g));

        MyRewardsWalletResponse response = walletUseCase.execute(RewardConstants.STATUS_AVAILABLE, RewardConstants.TYPE_ALL);

        assertThat(response).isNotNull();
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getVouchers()).hasSize(1);
        assertThat(response.getVouchers().get(0).getCode()).isEqualTo("SAVE100");
        assertThat(response.getGifts()).hasSize(1);
        assertThat(response.getGifts().get(0).getTitle()).isEqualTo("Tumbler Pink");
    }

    @Test
    @DisplayName("should calculate canAfford and pointsNeeded accurately in rewards catalog")
    void should_calculate_canAfford_and_pointsNeeded_in_catalog() {
        VoucherJpaEntity v1 = VoucherJpaEntity.builder()
                .code("V100")
                .title("100 NOK")
                .pointsCost(100)
                .discountType(RewardConstants.DISCOUNT_TYPE_FIXED)
                .discountValue(BigDecimal.valueOf(100))
                .minOrderValue(BigDecimal.ZERO)
                .validUntil(Instant.now().plusSeconds(86400))
                .build();
        v1.setId(UUID.randomUUID());

        VoucherJpaEntity v2 = VoucherJpaEntity.builder()
                .code("V300")
                .title("300 NOK")
                .pointsCost(1000)
                .discountType(RewardConstants.DISCOUNT_TYPE_FIXED)
                .discountValue(BigDecimal.valueOf(300))
                .minOrderValue(BigDecimal.valueOf(500))
                .validUntil(Instant.now().plusSeconds(86400))
                .build();
        v2.setId(UUID.randomUUID());

        LoyaltyGiftJpaEntity gift = LoyaltyGiftJpaEntity.builder()
                .title("Tumbler")
                .pointsCost(500)
                .stockQuantity(10)
                .build();
        gift.setId(UUID.randomUUID());

        when(voucherRepository.findByTenantIdAndActiveAndValidUntilAfter(eq(tenantId), eq(true), any(Instant.class)))
                .thenReturn(List.of(v1, v2));
        when(loyaltyGiftRepository.findByTenantIdAndActive(tenantId, true))
                .thenReturn(List.of(gift));

        RewardsCatalogResponse catalog = catalogUseCase.execute();

        assertThat(catalog.getMemberAvailablePoints()).isEqualTo(150);
        assertThat(catalog.getVouchers()).hasSize(2);
        assertThat(catalog.getVouchers().get(0).isCanAfford()).isTrue();
        assertThat(catalog.getVouchers().get(0).getPointsNeeded()).isEqualTo(0);
        assertThat(catalog.getVouchers().get(1).isCanAfford()).isFalse();
        assertThat(catalog.getVouchers().get(1).getPointsNeeded()).isEqualTo(850);

        assertThat(catalog.getGifts()).hasSize(1);
        assertThat(catalog.getGifts().get(0).isCanAfford()).isFalse();
        assertThat(catalog.getGifts().get(0).getPointsNeeded()).isEqualTo(350);
    }

    @Test
    @DisplayName("should count available vouchers and gifts for rewards summary badge")
    void should_count_available_vouchers_and_gifts_for_summary_badge() {
        when(userVoucherRepository.countByTenantIdAndMemberIdAndStatus(tenantId, memberId, RewardConstants.STATUS_AVAILABLE))
                .thenReturn(2L);
        when(userGiftRedemptionRepository.countByTenantIdAndMemberIdAndStatus(tenantId, memberId, RewardConstants.STATUS_PENDING))
                .thenReturn(1L);

        RewardsSummaryResponse summary = summaryUseCase.execute();

        assertThat(summary.getActiveVouchersCount()).isEqualTo(2);
        assertThat(summary.getAvailableGiftsCount()).isEqualTo(1);
        assertThat(summary.getTotalAvailableRewards()).isEqualTo(3);
    }
}

