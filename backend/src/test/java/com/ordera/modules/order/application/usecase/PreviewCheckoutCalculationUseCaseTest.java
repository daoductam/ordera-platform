package com.ordera.modules.order.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.MemberProfileJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.entity.TierDefinitionJpaEntity;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.order.application.service.CartService;
import com.ordera.modules.order.application.usecase.impl.PreviewCheckoutCalculationUseCaseImpl;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.domain.model.Cart;
import com.ordera.modules.order.domain.model.CartItem;
import com.ordera.modules.order.presentation.dto.request.PreviewCheckoutRequest;
import com.ordera.modules.order.presentation.dto.response.PreviewCheckoutResponse;
import com.ordera.modules.reward.domain.constant.RewardConstants;
import com.ordera.modules.reward.infrastructure.persistence.entity.VoucherJpaEntity;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PreviewCheckoutCalculationUseCase Unit Tests")
class PreviewCheckoutCalculationUseCaseTest {

    @Mock
    private CartService cartService;
    @Mock
    private VoucherJpaRepository voucherRepository;
    @Mock
    private LoyaltyGiftJpaRepository loyaltyGiftRepository;
    @Mock
    private MemberProfileJpaRepository memberProfileRepository;
    @Mock
    private TierDefinitionJpaRepository tierDefinitionRepository;

    @InjectMocks
    private PreviewCheckoutCalculationUseCaseImpl previewUseCase;

    private UUID tenantId;
    private UUID userId;
    private UUID cartId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        cartId = UUID.randomUUID();
    }

    @Test
    @DisplayName("should_calculate_checkout_preview_with_voucher_and_tip_accurately")
    void should_calculate_checkout_preview_with_voucher_and_tip_accurately() {
        // Arrange: Subtotal 250.00 NOK, Voucher 30% (-75.00 NOK), Tip 10.00 NOK -> Total 185.00 NOK
        CartItem item1 = CartItem.builder().id(UUID.randomUUID()).subtotalPrice(new BigDecimal("178.00")).quantity(2).build();
        CartItem item2 = CartItem.builder().id(UUID.randomUUID()).subtotalPrice(new BigDecimal("72.00")).quantity(1).build();

        Cart cart = Cart.builder()
                .id(cartId)
                .tenantId(tenantId)
                .userId(userId)
                .items(List.of(item1, item2))
                .subtotalAmount(new BigDecimal("250.00"))
                .build();

        VoucherJpaEntity voucher = VoucherJpaEntity.builder()
                .code("DISCOUNT30")
                .title("DISCOUNT30%")
                .discountType(RewardConstants.DISCOUNT_TYPE_PERCENT)
                .discountValue(new BigDecimal("30.00"))
                .minOrderValue(new BigDecimal("200.00"))
                .build();

        UUID tierId = UUID.randomUUID();
        MemberProfileJpaEntity profile = MemberProfileJpaEntity.builder()
                .userId(userId)
                .currentTierId(tierId)
                .build();
        TierDefinitionJpaEntity tier = TierDefinitionJpaEntity.builder()
                .earnPointsRate(new BigDecimal("1.0"))
                .build();

        PreviewCheckoutRequest request = PreviewCheckoutRequest.builder()
                .cartId(cartId)
                .voucherCode("DISCOUNT30")
                .tipAmount(new BigDecimal("10.00"))
                .pickupMethod("DINE_IN")
                .build();

        when(cartService.getCartDomain(tenantId, cartId)).thenReturn(cart);
        when(voucherRepository.findByTenantIdAndCode(tenantId, "DISCOUNT30")).thenReturn(Optional.of(voucher));
        when(memberProfileRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(Optional.of(profile));
        when(tierDefinitionRepository.findById(tierId)).thenReturn(Optional.of(tier));

        // Act
        PreviewCheckoutResponse response = previewUseCase.execute(tenantId, userId, request);

        // Assert: Subtotal 250, Discount 75, Tip 10, Total 185
        assertNotNull(response);
        assertEquals(new BigDecimal("250.00"), response.getSubtotalAmount());
        assertEquals(new BigDecimal("75.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("10.00"), response.getTipAmount());
        assertEquals(new BigDecimal("185.00"), response.getFinalTotalAmount());
        assertEquals(new BigDecimal("24.13"), response.getVatAmount());
        // Points = floor((250 - 75) * 1.0) = 175
        assertEquals(175, response.getPointsToEarn());
    }

    @Test
    @DisplayName("should_throw_exception_when_cart_is_empty")
    void should_throw_exception_when_cart_is_empty() {
        // Arrange
        Cart emptyCart = Cart.builder()
                .id(cartId)
                .tenantId(tenantId)
                .items(List.of())
                .subtotalAmount(BigDecimal.ZERO)
                .build();

        PreviewCheckoutRequest request = PreviewCheckoutRequest.builder().cartId(cartId).build();
        when(cartService.getCartDomain(tenantId, cartId)).thenReturn(emptyCart);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> previewUseCase.execute(tenantId, userId, request));
        assertEquals(OrderErrorCode.CART_EMPTY, ex.getErrorCode());
    }

    @Test
    @DisplayName("should_throw_exception_when_min_order_value_not_met")
    void should_throw_exception_when_min_order_value_not_met() {
        // Arrange: Subtotal 180.00 NOK, Voucher requires min 250.00 NOK
        CartItem item = CartItem.builder().id(UUID.randomUUID()).subtotalPrice(new BigDecimal("180.00")).quantity(1).build();
        Cart cart = Cart.builder()
                .id(cartId)
                .tenantId(tenantId)
                .items(List.of(item))
                .subtotalAmount(new BigDecimal("180.00"))
                .build();

        VoucherJpaEntity voucher = VoucherJpaEntity.builder()
                .code("DISCOUNT30")
                .minOrderValue(new BigDecimal("250.00"))
                .discountType(RewardConstants.DISCOUNT_TYPE_PERCENT)
                .discountValue(new BigDecimal("30.00"))
                .build();

        PreviewCheckoutRequest request = PreviewCheckoutRequest.builder()
                .cartId(cartId)
                .voucherCode("DISCOUNT30")
                .build();

        when(cartService.getCartDomain(tenantId, cartId)).thenReturn(cart);
        when(voucherRepository.findByTenantIdAndCode(tenantId, "DISCOUNT30")).thenReturn(Optional.of(voucher));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> previewUseCase.execute(tenantId, userId, request));
        assertEquals(OrderErrorCode.MINIMUM_ORDER_VALUE_NOT_MET, ex.getErrorCode());
    }
}

