package com.ordera.modules.order.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.order.application.usecase.SendEReceiptEmailUseCase;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.infrastructure.adapter.JavaMailReceiptAdapter;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ordera.modules.order.presentation.dto.response.SendEReceiptResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * UseCase xuất và gửi E-Receipt PDF qua Email (API 6.10).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendEReceiptEmailUseCaseImpl implements SendEReceiptEmailUseCase {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final JavaMailReceiptAdapter javaMailReceiptAdapter;

    @Override
    @Transactional
    public SendEReceiptResponse execute(UUID tenantId, UUID userId, UUID orderId, String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException(OrderErrorCode.INVALID_PICKUP_DETAILS, "Địa chỉ email không được để trống.");
        }

        OrderJpaEntity order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getUserId() != null && (userId == null || !order.getUserId().equals(userId))) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderItemJpaEntity> items = orderItemRepository.findByOrderId(order.getId());

        order.setEReceiptEmail(email.trim());
        orderRepository.save(order);

        javaMailReceiptAdapter.sendReceiptEmail(order, items, email.trim());

        return SendEReceiptResponse.builder()
                .sentToEmail(email.trim())
                .orderNumber(order.getOrderNumber())
                .dispatchedAt(Instant.now())
                .build();
    }
}

