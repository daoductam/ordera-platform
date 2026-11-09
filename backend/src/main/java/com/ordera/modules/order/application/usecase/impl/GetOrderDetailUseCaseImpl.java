package com.ordera.modules.order.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.order.application.usecase.GetOrderDetailUseCase;
import com.ordera.modules.order.domain.constant.OrderConstants;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.domain.model.OrderStatus;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemModifierJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemModifierJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ordera.modules.order.presentation.dto.response.*;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UseCase lấy thông tin chi tiết đơn hàng & timeline bếp (API 6.8).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetOrderDetailUseCaseImpl implements GetOrderDetailUseCase {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final OrderItemModifierJpaRepository modifierRepository;
    private final StoreRepository storeRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm")
            .withZone(ZoneId.of("Europe/Oslo"));

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse execute(UUID tenantId, UUID userId, UUID orderId) {
        OrderJpaEntity order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getUserId() != null && (userId == null || !order.getUserId().equals(userId))) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }

        StoreInfoResponse storeInfo = buildStoreInfo(tenantId, order.getStoreId());
        List<TimelineStepResponse> timeline = buildKitchenTimeline(order);
        List<OrderDetailItemResponse> items = buildItemDetails(order.getId());
        FinancialSummaryResponse financialSummary = buildFinancialSummary(order);

        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderCode(order.getOrderCode())
                .store(storeInfo)
                .status(order.getStatus())
                .timeline(timeline)
                .estimatedReadyMinutes(OrderConstants.DEFAULT_ESTIMATED_READY_MINUTES)
                .pickupMethod(order.getPickupMethod())
                .tableNumber(order.getTableNumber())
                .items(items)
                .financialSummary(financialSummary)
                .pointsEarned(order.getPointsEarned())
                .build();
    }

    private StoreInfoResponse buildStoreInfo(UUID tenantId, UUID storeId) {
        if (storeId != null) {
            Optional<StoreJpaEntity> store = storeRepository.findByIdAndTenantId(storeId, tenantId);
            if (store.isPresent()) {
                return StoreInfoResponse.builder()
                        .name(store.get().getName())
                        .address(store.get().getAddress())
                        .phoneNumber(store.get().getPhoneNumber())
                        .build();
            }
        }
        return StoreInfoResponse.builder()
                .name("Linn Sushi Sentrum")
                .address("Prinsens gate 10C, 0153 Oslo")
                .phoneNumber("+4722334455")
                .build();
    }

    private List<TimelineStepResponse> buildKitchenTimeline(OrderJpaEntity order) {
        String createdTime = order.getCreatedAt() != null ? TIME_FORMATTER.format(order.getCreatedAt()) : "--:--";
        String status = order.getStatus();

        boolean placedDone = true;
        boolean kitchenDone = OrderStatus.PREPARING.name().equalsIgnoreCase(status)
                || OrderStatus.READY.name().equalsIgnoreCase(status)
                || OrderStatus.COMPLETED.name().equalsIgnoreCase(status);
        boolean readyDone = OrderStatus.READY.name().equalsIgnoreCase(status)
                || OrderStatus.COMPLETED.name().equalsIgnoreCase(status);

        List<TimelineStepResponse> steps = new ArrayList<>();
        steps.add(new TimelineStepResponse(OrderConstants.TIMELINE_PLACED, createdTime, placedDone));
        steps.add(new TimelineStepResponse(OrderConstants.TIMELINE_IN_KITCHEN, createdTime, kitchenDone));
        steps.add(new TimelineStepResponse(OrderConstants.TIMELINE_READY_FOR_PICKUP, createdTime, readyDone));
        return steps;
    }

    private List<OrderDetailItemResponse> buildItemDetails(UUID orderId) {
        List<OrderItemJpaEntity> itemEntities = orderItemRepository.findByOrderId(orderId);
        if (itemEntities.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> itemIds = itemEntities.stream().map(OrderItemJpaEntity::getId).toList();
        List<OrderItemModifierJpaEntity> allModifiers = modifierRepository.findByOrderItemIdIn(itemIds);
        Map<UUID, List<OrderItemModifierJpaEntity>> modifierMap = allModifiers.stream()
                .collect(Collectors.groupingBy(OrderItemModifierJpaEntity::getOrderItemId));

        return itemEntities.stream().map(item -> {
            List<OrderItemModifierJpaEntity> mods = modifierMap.getOrDefault(item.getId(), Collections.emptyList());
            String modSummary = mods.stream()
                    .map(m -> m.getModifierGroupName() + ": " + m.getModifierName())
                    .collect(Collectors.joining(", "));
            return OrderDetailItemResponse.builder()
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .modifiersSummary(modSummary.isBlank() ? "Standard" : modSummary)
                    .build();
        }).toList();
    }

    private FinancialSummaryResponse buildFinancialSummary(OrderJpaEntity order) {
        return FinancialSummaryResponse.builder()
                .subtotalAmount(order.getSubtotalAmount())
                .discountAmount(order.getDiscountAmount())
                .tipAmount(order.getTipAmount())
                .vatAmount(order.getVatAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .build();
    }
}

