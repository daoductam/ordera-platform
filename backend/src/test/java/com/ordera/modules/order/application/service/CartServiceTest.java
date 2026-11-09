package com.ordera.modules.order.application.service;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.domain.model.Cart;
import com.ordera.modules.order.domain.model.CartItem;
import com.ordera.modules.order.infrastructure.adapter.RedisCartStorageAdapter;
import com.ordera.modules.order.infrastructure.mapper.OrderDtoMapper;
import com.ordera.modules.order.infrastructure.persistence.entity.CartJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.CartItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.CartJpaRepository;
import com.ordera.modules.order.presentation.dto.request.AddCartItemRequest;
import com.ordera.modules.order.presentation.dto.response.CartResponse;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock
    private RedisCartStorageAdapter redisCartStorage;
    @Mock
    private CartJpaRepository cartJpaRepository;
    @Mock
    private CartItemJpaRepository cartItemJpaRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductModifierRepository productModifierRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private OrderDtoMapper orderDtoMapper;

    @InjectMocks
    private CartService cartService;

    private UUID tenantId;
    private UUID userId;
    private UUID productId;
    private ProductJpaEntity product;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        product = ProductJpaEntity.builder()
                .name("Aurora Roll")
                .basePrice(new BigDecimal("89.00"))
                .available(true)
                .build();
        product.setId(productId);
        product.setTenantId(tenantId);
    }

    @Test
    @DisplayName("should_add_item_to_cart_successfully")
    void should_add_item_to_cart_successfully() {
        // Arrange
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .userId(userId)
                .items(new ArrayList<>())
                .subtotalAmount(BigDecimal.ZERO)
                .build();

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(product));
        when(redisCartStorage.getCartByUserId(tenantId, userId)).thenReturn(Optional.of(cart));
        when(cartJpaRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.empty());

        // Act
        CartResponse response = cartService.addItem(tenantId, userId, request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getTotalItemCount());
        assertEquals(new BigDecimal("178.00"), response.getSubtotalAmount());
        verify(redisCartStorage, atLeastOnce()).saveCart(any());
    }

    @Test
    @DisplayName("should_throw_exception_when_product_not_found")
    void should_throw_exception_when_product_not_found() {
        // Arrange
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();

        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> cartService.addItem(tenantId, userId, request));
        assertEquals(OrderErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("should_throw_exception_when_comment_exceeds_max_length")
    void should_throw_exception_when_comment_exceeds_max_length() {
        // Arrange: 201 characters comment
        String longComment = "a".repeat(201);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> cartService.updateComment(tenantId, userId, longComment));
        assertEquals(OrderErrorCode.COMMENT_TOO_LONG, ex.getErrorCode());
    }

    @Test
    @DisplayName("should_update_item_quantity_and_delete_when_zero")
    void should_update_item_quantity_and_delete_when_zero() {
        // Arrange
        UUID itemId = UUID.randomUUID();
        CartItem item = CartItem.builder()
                .id(itemId)
                .productId(productId)
                .productName("Aurora")
                .unitPrice(new BigDecimal("89.00"))
                .quantity(2)
                .subtotalPrice(new BigDecimal("178.00"))
                .build();

        Cart cart = Cart.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .userId(userId)
                .items(new ArrayList<>(java.util.List.of(item)))
                .subtotalAmount(new BigDecimal("178.00"))
                .build();

        when(redisCartStorage.getCartByUserId(tenantId, userId)).thenReturn(Optional.of(cart));
        when(cartJpaRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.empty());

        // Act: Delete by setting quantity to 0
        CartResponse response = cartService.updateItemQuantity(tenantId, userId, itemId, 0);

        // Assert
        assertEquals(0, response.getTotalItemCount());
        assertEquals(new BigDecimal("0.00"), response.getSubtotalAmount());
    }
}
