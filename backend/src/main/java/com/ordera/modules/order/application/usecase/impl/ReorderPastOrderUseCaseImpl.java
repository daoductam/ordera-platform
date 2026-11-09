package com.ordera.modules.order.application.usecase.impl;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.order.application.service.CartService;
import com.ordera.modules.order.application.usecase.ReorderPastOrderUseCase;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderItemJpaRepository;
import com.ordera.modules.order.infrastructure.persistence.repository.OrderJpaRepository;
import com.ordera.modules.order.presentation.dto.request.AddCartItemRequest;
import com.ordera.modules.order.presentation.dto.response.CartResponse;
import com.ordera.modules.order.presentation.dto.response.ReorderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * UseCase đặt lại đơn hàng cũ nhanh chóng 1-click (API 6.9).
 * Sao chép toàn bộ món ăn từ đơn cũ vào giỏ hàng mới.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReorderPastOrderUseCaseImpl implements ReorderPastOrderUseCase {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public ReorderResponse execute(UUID tenantId, UUID userId, UUID orderId) {
        OrderJpaEntity order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (userId != null && order.getUserId() != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderItemJpaEntity> pastItems = orderItemRepository.findByOrderId(order.getId());
        if (pastItems.isEmpty()) {
            throw new BusinessException(OrderErrorCode.CART_EMPTY, "Đơn hàng cũ không chứa món ăn nào.");
        }

        CartResponse cartResponse = null;
        for (OrderItemJpaEntity item : pastItems) {
            ProductJpaEntity product = productRepository.findByIdAndTenantId(item.getProductId(), tenantId)
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.STOCK_UNAVAILABLE,
                            "Món '" + item.getProductName() + "' đã ngừng kinh doanh."));

            if (!product.isAvailable()) {
                throw new BusinessException(OrderErrorCode.STOCK_UNAVAILABLE,
                        "Món '" + item.getProductName() + "' hiện đang tạm hết hàng.");
            }

            AddCartItemRequest addReq = AddCartItemRequest.builder()
                    .storeId(order.getStoreId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .selectedModifierIds(new ArrayList<>())
                    .specialInstructions(item.getSpecialInstruction())
                    .build();

            cartResponse = cartService.addItem(tenantId, userId, addReq);
        }

        return ReorderResponse.builder()
                .cartId(cartResponse != null ? cartResponse.getCartId() : null)
                .itemCount(cartResponse != null ? cartResponse.getTotalItemCount() : 0)
                .subtotalAmount(cartResponse != null ? cartResponse.getSubtotalAmount() : null)
                .build();
    }
}
