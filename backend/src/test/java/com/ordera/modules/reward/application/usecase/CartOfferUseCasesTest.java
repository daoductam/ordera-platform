package com.ordera.modules.reward.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.reward.application.port.CartOfferPort;
import com.ordera.modules.reward.application.usecase.impl.*;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.domain.exception.RewardErrorCode;
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
import com.ordera.modules.reward.presentation.dto.*;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartOfferUseCasesTest {

    @Mock
    private CartOfferPort cartOfferPort;
    @Mock
    private MemberProfileJpaRepository memberProfileRepository;
    @Mock
    private UserVoucherJpaRepository userVoucherRepository;
    @Mock
    private VoucherJpaRepository voucherRepository;
    @Mock
    private LoyaltyGiftJpaRepository loyaltyGiftRepository;
    @Mock
    private UserGiftRedemptionJpaRepository userGiftRedemptionRepository;

    private final RewardDtoMapper rewardDtoMapper = new RewardDtoMapperImpl();

    private GetAvailableOffersForCartUseCaseImpl availableOffersUseCase;
    private ValidatePromoCodeUseCaseImpl validatePromoCodeUseCase;
    private ApplyVoucherToCartUseCaseImpl applyVoucherUseCase;
    private RemoveVoucherFromCartUseCaseImpl removeVoucherUseCase;
    private ApplyGiftToCartUseCaseImpl applyGiftUseCase;
    private RemoveGiftFromCartUseCaseImpl removeGiftUseCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID voucherId = UUID.randomUUID();
    private final UUID giftId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );

        availableOffersUseCase = new GetAvailableOffersForCartUseCaseImpl(
                cartOfferPort, memberProfileRepository, userVoucherRepository, voucherRepository, loyaltyGiftRepository, rewardDtoMapper
        );
        validatePromoCodeUseCase = new ValidatePromoCodeUseCaseImpl(
                cartOfferPort, voucherRepository
        );
        applyVoucherUseCase = new ApplyVoucherToCartUseCaseImpl(
                cartOfferPort, memberProfileRepository, voucherRepository, userVoucherRepository
        );
        removeVoucherUseCase = new RemoveVoucherFromCartUseCaseImpl(
                cartOfferPort, memberProfileRepository, userVoucherRepository
        );
        applyGiftUseCase = new ApplyGiftToCartUseCaseImpl(
                cartOfferPort, memberProfileRepository, loyaltyGiftRepository, userGiftRedemptionRepository
        );
        removeGiftUseCase = new RemoveGiftFromCartUseCaseImpl(
                cartOfferPort, memberProfileRepository, userGiftRedemptionRepository
        );

        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder().userId(userId).build();
        profile.setId(memberId);
        lenient().when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should evaluate applicable offers based on cart subtotal")
    void should_evaluate_applicable_offers_based_on_cart_subtotal() {
        CartOfferPort.CartOfferDetails cart = new CartOfferPort.CartOfferDetails(
                cartId, tenantId, UUID.randomUUID(), userId, BigDecimal.valueOf(250), null, null
        );
        when(cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)).thenReturn(Optional.of(cart));

        UserVoucherJpaEntity uv1 = UserVoucherJpaEntity.builder().voucherId(voucherId).status(RewardConstants.STATUS_AVAILABLE).build();
        VoucherJpaEntity v1 = VoucherJpaEntity.builder()
                .code("SAVE100")
                .discountType(RewardConstants.DISCOUNT_TYPE_FIXED)
                .discountValue(BigDecimal.valueOf(100))
                .minOrderValue(BigDecimal.valueOf(200))
                .active(true)
                .build();
        v1.setId(voucherId);

        when(userVoucherRepository.findByTenantIdAndMemberId(tenantId, memberId)).thenReturn(List.of(uv1));
        when(voucherRepository.findAllById(List.of(voucherId))).thenReturn(List.of(v1));

        AvailableOffersResponse offers = availableOffersUseCase.execute(cartId);

        assertThat(offers.getSubtotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(offers.getVouchers()).hasSize(1);
        assertThat(offers.getVouchers().get(0).isApplicable()).isTrue();
    }

    @Test
    @DisplayName("should validate promo code and calculate discount amount")
    void should_validate_promo_code_and_calculate_discount() {
        CartOfferPort.CartOfferDetails cart = new CartOfferPort.CartOfferDetails(
                cartId, tenantId, UUID.randomUUID(), userId, BigDecimal.valueOf(300), null, null
        );
        when(cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)).thenReturn(Optional.of(cart));

        VoucherJpaEntity voucher = VoucherJpaEntity.builder()
                .code("DISCOUNT30")
                .discountType(RewardConstants.DISCOUNT_TYPE_PERCENT)
                .discountValue(BigDecimal.valueOf(30))
                .minOrderValue(BigDecimal.valueOf(100))
                .validUntil(Instant.now().plusSeconds(86400))
                .active(true)
                .build();
        when(voucherRepository.findByTenantIdAndCode(tenantId, "DISCOUNT30")).thenReturn(Optional.of(voucher));

        ValidatePromoCodeResponse response = validatePromoCodeUseCase.execute(new ValidatePromoCodeRequest("DISCOUNT30", cartId));

        assertThat(response.getCode()).isEqualTo("DISCOUNT30");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(90)); // 30% of 300 = 90
    }

    @Test
    @DisplayName("should throw PROMO_CODE_INELIGIBLE when subtotal does not meet minOrderValue")
    void should_throw_PROMO_CODE_INELIGIBLE_when_subtotal_low() {
        CartOfferPort.CartOfferDetails cart = new CartOfferPort.CartOfferDetails(
                cartId, tenantId, UUID.randomUUID(), userId, BigDecimal.valueOf(100), null, null
        );
        when(cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)).thenReturn(Optional.of(cart));

        VoucherJpaEntity voucher = VoucherJpaEntity.builder()
                .code("SAVE50")
                .minOrderValue(BigDecimal.valueOf(200))
                .validUntil(Instant.now().plusSeconds(86400))
                .active(true)
                .build();
        when(voucherRepository.findByTenantIdAndCode(tenantId, "SAVE50")).thenReturn(Optional.of(voucher));

        assertThatThrownBy(() -> validatePromoCodeUseCase.execute(new ValidatePromoCodeRequest("SAVE50", cartId)))
                .isInstanceOf(BusinessException.class)
                .matches(e -> ((BusinessException) e).getErrorCode() == RewardErrorCode.PROMO_CODE_INELIGIBLE);
    }

    @Test
    @DisplayName("should apply voucher to cart, mark user voucher as IN_USE, and enforce max 1 voucher rule")
    void should_apply_voucher_and_enforce_combination_rule() {
        CartOfferPort.CartOfferDetails cart = new CartOfferPort.CartOfferDetails(
                cartId, tenantId, UUID.randomUUID(), userId, BigDecimal.valueOf(300), null, null
        );
        when(cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)).thenReturn(Optional.of(cart));

        VoucherJpaEntity voucher = VoucherJpaEntity.builder()
                .code("OFF30")
                .title("30% Off")
                .discountType(RewardConstants.DISCOUNT_TYPE_PERCENT)
                .discountValue(BigDecimal.valueOf(30))
                .minOrderValue(BigDecimal.valueOf(100))
                .validUntil(Instant.now().plusSeconds(86400))
                .active(true)
                .build();
        voucher.setId(voucherId);

        UserVoucherJpaEntity uv = UserVoucherJpaEntity.builder()
                .memberId(memberId)
                .voucherId(voucherId)
                .status(RewardConstants.STATUS_AVAILABLE)
                .build();

        when(voucherRepository.findByTenantIdAndCode(tenantId, "OFF30")).thenReturn(Optional.of(voucher));
        when(userVoucherRepository.findByTenantIdAndMemberIdAndVoucherIdAndStatus(tenantId, memberId, voucherId, RewardConstants.STATUS_AVAILABLE))
                .thenReturn(Optional.of(uv));

        CartVoucherCalculationResponse result = applyVoucherUseCase.execute(new ApplyVoucherRequest(cartId, "OFF30"));

        assertThat(result.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(90));
        assertThat(uv.getStatus()).isEqualTo(RewardConstants.STATUS_IN_USE);
        assertThat(uv.getAppliedOrderId()).isEqualTo(cartId);
        verify(cartOfferPort).updateAppliedVoucher(cartId, tenantId, "OFF30");

        // Test combination rule: attempt to apply a second different voucher to a cart that already has OFF30
        CartOfferPort.CartOfferDetails cartWithVoucher = new CartOfferPort.CartOfferDetails(
                cartId, tenantId, UUID.randomUUID(), userId, BigDecimal.valueOf(300), "OFF30", null
        );
        when(cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)).thenReturn(Optional.of(cartWithVoucher));

        assertThatThrownBy(() -> applyVoucherUseCase.execute(new ApplyVoucherRequest(cartId, "ANOTHER_CODE")))
                .isInstanceOf(BusinessException.class)
                .matches(e -> ((BusinessException) e).getErrorCode() == RewardErrorCode.INVALID_REWARD_COMBINATION);
    }

    @Test
    @DisplayName("should remove voucher from cart and revert user voucher status to AVAILABLE")
    void should_remove_voucher_and_revert_status() {
        CartOfferPort.CartOfferDetails cart = new CartOfferPort.CartOfferDetails(
                cartId, tenantId, UUID.randomUUID(), userId, BigDecimal.valueOf(300), "OFF30", null
        );
        when(cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)).thenReturn(Optional.of(cart));

        UserVoucherJpaEntity uv = UserVoucherJpaEntity.builder()
                .status(RewardConstants.STATUS_IN_USE)
                .appliedOrderId(cartId)
                .build();
        when(userVoucherRepository.findByAppliedOrderId(cartId)).thenReturn(Optional.of(uv));

        CartVoucherCalculationResponse result = removeVoucherUseCase.execute(new RemoveVoucherRequest(cartId, "OFF30"));

        assertThat(result.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(uv.getStatus()).isEqualTo(RewardConstants.STATUS_AVAILABLE);
        assertThat(uv.getAppliedOrderId()).isNull();
        verify(cartOfferPort).clearAppliedVoucher(cartId, tenantId);
    }

    @Test
    @DisplayName("should apply physical gift to cart and mark status as SELECTED")
    void should_apply_gift_and_enforce_single_gift_rule() {
        CartOfferPort.CartOfferDetails cart = new CartOfferPort.CartOfferDetails(
                cartId, tenantId, UUID.randomUUID(), userId, BigDecimal.valueOf(300), null, null
        );
        when(cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)).thenReturn(Optional.of(cart));

        LoyaltyGiftJpaEntity gift = LoyaltyGiftJpaEntity.builder().title("Tumbler").build();
        gift.setId(giftId);
        when(loyaltyGiftRepository.findByIdAndTenantId(giftId, tenantId)).thenReturn(Optional.of(gift));

        UserGiftRedemptionJpaEntity redemption = UserGiftRedemptionJpaEntity.builder()
                .giftId(giftId)
                .status(RewardConstants.STATUS_PENDING)
                .build();
        when(userGiftRedemptionRepository.findByTenantIdAndMemberIdAndStatus(tenantId, memberId, RewardConstants.STATUS_PENDING))
                .thenReturn(List.of(redemption));

        CartGiftCalculationResponse result = applyGiftUseCase.execute(new ApplyGiftRequest(cartId, giftId));

        assertThat(result.getSelectedGift().getStatus()).isEqualTo(RewardConstants.STATUS_SELECTED);
        assertThat(redemption.getStatus()).isEqualTo(RewardConstants.STATUS_SELECTED);
        verify(cartOfferPort).updateAppliedGift(cartId, tenantId, giftId);

        // Test combination rule: attempt to apply a second different gift
        UUID anotherGiftId = UUID.randomUUID();
        CartOfferPort.CartOfferDetails cartWithGift = new CartOfferPort.CartOfferDetails(
                cartId, tenantId, UUID.randomUUID(), userId, BigDecimal.valueOf(300), null, giftId
        );
        when(cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)).thenReturn(Optional.of(cartWithGift));

        assertThatThrownBy(() -> applyGiftUseCase.execute(new ApplyGiftRequest(cartId, anotherGiftId)))
                .isInstanceOf(BusinessException.class)
                .matches(e -> ((BusinessException) e).getErrorCode() == RewardErrorCode.INVALID_REWARD_COMBINATION);
    }

    @Test
    @DisplayName("should remove gift from cart and revert redemption status to PENDING")
    void should_remove_gift_and_revert_status() {
        CartOfferPort.CartOfferDetails cart = new CartOfferPort.CartOfferDetails(
                cartId, tenantId, UUID.randomUUID(), userId, BigDecimal.valueOf(300), null, giftId
        );
        when(cartOfferPort.findCartByIdAndTenantId(cartId, tenantId)).thenReturn(Optional.of(cart));

        UserGiftRedemptionJpaEntity redemption = UserGiftRedemptionJpaEntity.builder()
                .giftId(giftId)
                .status(RewardConstants.STATUS_SELECTED)
                .appliedOrderId(cartId)
                .build();
        when(userGiftRedemptionRepository.findByTenantIdAndAppliedOrderId(tenantId, cartId)).thenReturn(Optional.of(redemption));

        CartGiftCalculationResponse result = removeGiftUseCase.execute(new RemoveGiftRequest(cartId, giftId));

        assertThat(result.getSelectedGift()).isNull();
        assertThat(redemption.getStatus()).isEqualTo(RewardConstants.STATUS_PENDING);
        assertThat(redemption.getAppliedOrderId()).isNull();
        verify(cartOfferPort).clearAppliedGift(cartId, tenantId);
    }
}
