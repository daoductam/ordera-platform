package com.ordera.modules.order.application.usecase;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.order.application.usecase.impl.GetMyOrdersUseCaseImpl;
import com.ordera.modules.order.application.usecase.impl.GetOrderDetailUseCaseImpl;
import com.ordera.modules.order.application.usecase.impl.SendEReceiptEmailUseCaseImpl;
import com.ordera.modules.order.domain.constant.OrderConstants;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.domain.model.OrderStatus;
import com.ordera.modules.order.infrastructure.adapter.JavaMailReceiptAdapter;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemModifierJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemModifierJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ordera.modules.order.presentation.dto.response.OrderDetailResponse;
import com.ordera.modules.order.presentation.dto.response.OrderSummaryResponse;
import com.ordera.modules.order.presentation.dto.response.SendEReceiptResponse;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderTrackingAndReceiptUseCases Unit Tests")
class OrderTrackingAndReceiptUseCasesTest {

    @Mock
    private OrderJpaRepository orderRepository;

    @Mock
    private OrderItemJpaRepository orderItemRepository;

    @Mock
    private OrderItemModifierJpaRepository modifierRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private JavaMailReceiptAdapter javaMailReceiptAdapter;

    private GetOrderDetailUseCase getOrderDetailUseCase;
    private GetMyOrdersUseCase getMyOrdersUseCase;
    private SendEReceiptEmailUseCase sendEReceiptEmailUseCase;

    private UUID tenantId;
    private UUID memberUserId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        memberUserId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        getOrderDetailUseCase = new GetOrderDetailUseCaseImpl(
                orderRepository, orderItemRepository, modifierRepository, storeRepository);

        getMyOrdersUseCase = new GetMyOrdersUseCaseImpl(
                orderRepository, orderItemRepository, storeRepository);

        sendEReceiptEmailUseCase = new SendEReceiptEmailUseCaseImpl(
                orderRepository, orderItemRepository, javaMailReceiptAdapter);
    }

    @Test
    @DisplayName("GetOrderDetail - should reject anonymous request when order belongs to a registered member (IDOR prevention)")
    void should_reject_anonymous_request_when_order_belongs_to_member() {
        OrderJpaEntity order = OrderJpaEntity.builder()
                .userId(memberUserId)
                .orderNumber("#001")
                .status(OrderStatus.PREPARING.name())
                .build();
        order.setId(orderId);
        order.setTenantId(tenantId);

        when(orderRepository.findByIdAndTenantId(orderId, tenantId)).thenReturn(Optional.of(order));

        // Act & Assert: Anonymous user (userId == null) attempting to view registered member's order
        assertThatThrownBy(() -> getOrderDetailUseCase.execute(tenantId, null, orderId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Test
    @DisplayName("SendEReceiptEmail - should reject anonymous request when order belongs to a registered member (IDOR prevention)")
    void should_reject_anonymous_receipt_email_when_order_belongs_to_member() {
        OrderJpaEntity order = OrderJpaEntity.builder()
                .userId(memberUserId)
                .orderNumber("#001")
                .build();
        order.setId(orderId);
        order.setTenantId(tenantId);

        when(orderRepository.findByIdAndTenantId(orderId, tenantId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> sendEReceiptEmailUseCase.execute(tenantId, null, orderId, "attacker@test.com"))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Test
    @DisplayName("GetOrderDetail - should return rich details with batch modifier fetching when authorized")
    void should_return_order_details_with_batch_modifiers() {
        UUID storeId = UUID.randomUUID();
        OrderJpaEntity order = OrderJpaEntity.builder()
                .userId(memberUserId)
                .storeId(storeId)
                .orderNumber("#001")
                .orderCode("ORD-20261109-1234")
                .status(OrderStatus.PREPARING.name())
                .subtotalAmount(new BigDecimal("150.00"))
                .discountAmount(BigDecimal.ZERO)
                .tipAmount(new BigDecimal("20.00"))
                .vatAmount(new BigDecimal("22.17"))
                .totalAmount(new BigDecimal("170.00"))
                .pointsEarned(150)
                .pickupMethod("COUNTER")
                .createdAt(Instant.now())
                .build();
        order.setId(orderId);
        order.setTenantId(tenantId);

        UUID itemId = UUID.randomUUID();
        OrderItemJpaEntity item = OrderItemJpaEntity.builder()
                .orderId(orderId)
                .productId(UUID.randomUUID())
                .productName("Salmon Nigiri 4 pcs")
                .unitPrice(new BigDecimal("75.00"))
                .quantity(2)
                .itemTotal(new BigDecimal("150.00"))
                .build();
        item.setId(itemId);

        OrderItemModifierJpaEntity modifier = OrderItemModifierJpaEntity.builder()
                .orderItemId(itemId)
                .modifierGroupName("Spicy Level")
                .modifierName("Extra Wasabi")
                .extraPrice(BigDecimal.ZERO)
                .build();

        StoreJpaEntity store = StoreJpaEntity.builder()
                .name("Linn Sushi Sentrum")
                .address("Prinsens gate 10C, 0153 Oslo")
                .phoneNumber("+4722334455")
                .build();
        store.setId(storeId);
        store.setTenantId(tenantId);

        when(orderRepository.findByIdAndTenantId(orderId, tenantId)).thenReturn(Optional.of(order));
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of(item));
        when(modifierRepository.findByOrderItemIdIn(List.of(itemId))).thenReturn(List.of(modifier));

        // Act
        OrderDetailResponse response = getOrderDetailUseCase.execute(tenantId, memberUserId, orderId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("#001");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getModifiersSummary()).contains("Extra Wasabi");
        verify(modifierRepository, times(1)).findByOrderItemIdIn(List.of(itemId));
        verify(modifierRepository, never()).findByOrderItemId(any());
    }

    @Test
    @DisplayName("GetMyOrders - should batch fetch items and stores for all orders in page (No N+1)")
    void should_batch_fetch_orders_without_n_plus_one() {
        UUID storeId = UUID.randomUUID();
        OrderJpaEntity order1 = OrderJpaEntity.builder()
                .userId(memberUserId)
                .storeId(storeId)
                .orderNumber("#001")
                .orderCode("ORD-20261109-0001")
                .status(OrderStatus.PREPARING.name())
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .build();
        order1.setId(UUID.randomUUID());
        order1.setTenantId(tenantId);

        OrderJpaEntity order2 = OrderJpaEntity.builder()
                .userId(memberUserId)
                .storeId(storeId)
                .orderNumber("#002")
                .orderCode("ORD-20261109-0002")
                .status(OrderStatus.READY.name())
                .totalAmount(new BigDecimal("200.00"))
                .createdAt(Instant.now())
                .build();
        order2.setId(UUID.randomUUID());
        order2.setTenantId(tenantId);

        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findByTenantIdAndUserIdAndStatusInOrderByCreatedAtDesc(
                eq(tenantId), eq(memberUserId), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(order1, order2), pageable, 2));

        OrderItemJpaEntity item1 = OrderItemJpaEntity.builder()
                .orderId(order1.getId())
                .productName("Sake Maki")
                .quantity(1)
                .build();
        OrderItemJpaEntity item2 = OrderItemJpaEntity.builder()
                .orderId(order2.getId())
                .productName("Tempura Ebi Roll")
                .quantity(2)
                .build();

        when(orderItemRepository.findByOrderIdIn(List.of(order1.getId(), order2.getId())))
                .thenReturn(List.of(item1, item2));

        StoreJpaEntity store = StoreJpaEntity.builder()
                .name("Linn Sushi Sentrum")
                .build();
        store.setId(storeId);
        store.setTenantId(tenantId);

        when(storeRepository.findAllById(List.of(storeId))).thenReturn(List.of(store));

        // Act
        Page<OrderSummaryResponse> page = getMyOrdersUseCase.execute(tenantId, memberUserId, OrderConstants.TAB_ACTIVE, pageable);

        // Assert
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getFirstItemName()).isEqualTo("Sake Maki");
        assertThat(page.getContent().get(1).getFirstItemName()).isEqualTo("Tempura Ebi Roll");
        verify(orderItemRepository, times(1)).findByOrderIdIn(any());
        verify(storeRepository, times(1)).findAllById(any());
        verify(storeRepository, never()).findByIdAndTenantId(any(), any());
    }
}
