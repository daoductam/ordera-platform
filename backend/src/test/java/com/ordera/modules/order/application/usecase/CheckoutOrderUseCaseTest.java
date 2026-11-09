package com.ordera.modules.order.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.loyalty.application.usecase.AccruePointsUseCase;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.MemberProfileJpaRepository;
import com.ordera.modules.loyalty.infrastructure.persistence.repository.TierDefinitionJpaRepository;
import com.ordera.modules.order.application.service.CartService;
import com.ordera.modules.order.application.usecase.impl.CheckoutOrderUseCaseImpl;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.domain.model.Cart;
import com.ordera.modules.order.domain.model.CartItem;
import com.ordera.modules.order.infrastructure.adapter.JavaMailReceiptAdapter;
import com.ordera.modules.order.infrastructure.adapter.RedisCartStorageAdapter;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemModifierJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ordera.modules.order.presentation.dto.request.CheckoutOrderRequest;
import com.ordera.modules.order.presentation.dto.response.CheckoutOrderResponse;
import com.ordera.modules.reward.infrastructure.persistence.repository.LoyaltyGiftJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.UserVoucherJpaRepository;
import com.ordera.modules.reward.infrastructure.persistence.repository.VoucherJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutOrderUseCase Unit Tests")
class CheckoutOrderUseCaseTest {

    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RedisCartStorageAdapter redisCartStorage;
    @Mock
    private CartService cartService;
    @Mock
    private OrderJpaRepository orderRepository;
    @Mock
    private OrderItemJpaRepository orderItemRepository;
    @Mock
    private OrderItemModifierJpaRepository orderItemModifierRepository;
    @Mock
    private com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierRepository productModifierRepository;
    @Mock
    private VoucherJpaRepository voucherRepository;
    @Mock
    private UserVoucherJpaRepository userVoucherRepository;
    @Mock
    private LoyaltyGiftJpaRepository loyaltyGiftRepository;
    @Mock
    private MemberProfileJpaRepository memberProfileRepository;
    @Mock
    private TierDefinitionJpaRepository tierDefinitionRepository;
    @Mock
    private AccruePointsUseCase accruePointsUseCase;
    @Mock
    private JavaMailReceiptAdapter javaMailReceiptAdapter;

    @Mock
    private RLock rLock;

    @InjectMocks
    private CheckoutOrderUseCaseImpl checkoutUseCase;

    private UUID tenantId;
    private UUID userId;
    private UUID cartId;
    private CheckoutOrderRequest request;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        cartId = UUID.randomUUID();

        request = CheckoutOrderRequest.builder()
                .cartId(cartId)
                .pickupMethod("DINE_IN")
                .tableNumber("Table 05")
                .pickupPersonName("David Nguyen")
                .pickupPersonPhone("+4797362748")
                .paymentMethod("VIPPS")
                .tipAmount(new BigDecimal("10.00"))
                .eReceiptEmail("david@example.com")
                .build();
    }

    @Test
    @DisplayName("should_checkout_order_successfully_with_lock_and_cart_cleared")
    void should_checkout_order_successfully_with_lock_and_cart_cleared() throws Exception {
        // Arrange
        String idempotencyKey = "idem-test-12345";
        CartItem item = CartItem.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .productName("Aurora")
                .unitPrice(new BigDecimal("89.00"))
                .quantity(2)
                .subtotalPrice(new BigDecimal("178.00"))
                .build();

        Cart cart = Cart.builder()
                .id(cartId)
                .tenantId(tenantId)
                .userId(userId)
                .storeId(UUID.randomUUID())
                .items(List.of(item))
                .subtotalAmount(new BigDecimal("178.00"))
                .build();

        when(redisCartStorage.getIdempotentOrderId(tenantId, idempotencyKey)).thenReturn(Optional.empty());
        when(redissonClient.getLock(any(String.class))).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(cartService.getCartDomain(tenantId, cartId)).thenReturn(cart);

        OrderJpaEntity savedOrder = OrderJpaEntity.builder()
                .orderNumber("#005")
                .orderCode("ORD-20261105-0891")
                .subtotalAmount(new BigDecimal("178.00"))
                .discountAmount(BigDecimal.ZERO)
                .tipAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("188.00"))
                .pointsEarned(178)
                .pickupMethod("DINE_IN")
                .status("PREPARING")
                .paymentStatus("PAID")
                .paymentMethod("VIPPS")
                .build();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setTenantId(tenantId);

        when(orderRepository.save(any(OrderJpaEntity.class))).thenReturn(savedOrder);
        when(orderItemRepository.save(any(OrderItemJpaEntity.class))).thenReturn(new OrderItemJpaEntity());

        // Act
        CheckoutOrderResponse response = checkoutUseCase.execute(tenantId, userId, idempotencyKey, request);

        // Assert
        assertNotNull(response);
        assertEquals("#005", response.getOrderNumber());
        assertEquals("ORD-20261105-0891", response.getOrderCode());
        assertEquals("PREPARING", response.getStatus());

        verify(rLock).unlock();
        verify(cartService).clearCart(tenantId, cartId, userId);
        verify(redisCartStorage).saveIdempotencyRecord(eq(tenantId), eq(idempotencyKey), any());
    }

    @Test
    @DisplayName("should_throw_exception_when_idempotency_key_already_exists")
    void should_throw_exception_when_idempotency_key_already_exists() {
        // Arrange
        String idempotencyKey = "idem-duplicate-key";
        when(redisCartStorage.getIdempotentOrderId(tenantId, idempotencyKey)).thenReturn(Optional.of("existing-order-id"));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> checkoutUseCase.execute(tenantId, userId, idempotencyKey, request));
        assertEquals(OrderErrorCode.IDEMPOTENT_OPERATION_CONFLICT, ex.getErrorCode());
    }
}
