package com.ordera.modules.order.application.usecase.impl;

import com.ordera.modules.order.application.usecase.GetMyOrdersUseCase;
import com.ordera.modules.order.domain.constant.OrderConstants;
import com.ordera.modules.order.domain.model.OrderStatus;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ordera.modules.order.presentation.dto.response.OrderSummaryResponse;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * UseCase lấy danh sách đơn hàng của khách hàng theo tab ACTIVE hoặc PAST (API 6.7).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetMyOrdersUseCaseImpl implements GetMyOrdersUseCase {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final StoreRepository storeRepository;

    private static final List<String> ACTIVE_STATUSES = List.of(
            OrderStatus.PENDING.name(),
            OrderStatus.CONFIRMED.name(),
            OrderStatus.PREPARING.name(),
            OrderStatus.READY.name()
    );

    private static final List<String> PAST_STATUSES = List.of(
            OrderStatus.COMPLETED.name(),
            OrderStatus.CANCELLED.name()
    );

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> execute(UUID tenantId, UUID userId, String tab, Pageable pageable) {
        List<String> targetStatuses = OrderConstants.TAB_PAST.equalsIgnoreCase(tab)
                ? PAST_STATUSES
                : ACTIVE_STATUSES;

        Page<OrderJpaEntity> ordersPage = orderRepository
                .findByTenantIdAndUserIdAndStatusInOrderByCreatedAtDesc(tenantId, userId, targetStatuses, pageable);

        List<OrderJpaEntity> orders = ordersPage.getContent();
        if (orders.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, ordersPage.getTotalElements());
        }

        List<UUID> orderIds = orders.stream().map(OrderJpaEntity::getId).toList();
        List<OrderItemJpaEntity> allItems = orderItemRepository.findByOrderIdIn(orderIds);
        Map<UUID, List<OrderItemJpaEntity>> orderItemsMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItemJpaEntity::getOrderId));

        List<UUID> storeIds = orders.stream()
                .map(OrderJpaEntity::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, String> storeNameMap = storeRepository.findAllById(storeIds).stream()
                .filter(store -> tenantId.equals(store.getTenantId()))
                .collect(Collectors.toMap(StoreJpaEntity::getId, StoreJpaEntity::getName, (existing, replacement) -> existing));

        List<OrderSummaryResponse> summaries = orders.stream()
                .map(order -> mapToSummary(order, orderItemsMap, storeNameMap))
                .toList();

        return new PageImpl<>(summaries, pageable, ordersPage.getTotalElements());
    }

    private OrderSummaryResponse mapToSummary(
            OrderJpaEntity order,
            Map<UUID, List<OrderItemJpaEntity>> orderItemsMap,
            Map<UUID, String> storeNameMap) {

        String storeName = order.getStoreId() != null
                ? storeNameMap.getOrDefault(order.getStoreId(), "Linn Sushi Sentrum")
                : "Linn Sushi Sentrum";

        List<OrderItemJpaEntity> items = orderItemsMap.getOrDefault(order.getId(), Collections.emptyList());
        int itemCount = items.stream().mapToInt(OrderItemJpaEntity::getQuantity).sum();
        String firstItemName = !items.isEmpty() ? items.get(0).getProductName() : "Order";

        return OrderSummaryResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderCode(order.getOrderCode())
                .storeName(storeName)
                .pickupMethod(order.getPickupMethod())
                .totalAmount(order.getTotalAmount())
                .itemCount(itemCount)
                .firstItemName(firstItemName)
                .status(order.getStatus())
                .estimatedReadyMinutes(OrderConstants.DEFAULT_ESTIMATED_READY_MINUTES)
                .createdAt(order.getCreatedAt())
                .build();
    }
}

