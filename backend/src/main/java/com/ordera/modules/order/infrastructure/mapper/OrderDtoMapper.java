package com.ordera.modules.order.infrastructure.mapper;

import com.ordera.modules.order.domain.model.Cart;
import com.ordera.modules.order.domain.model.CartItem;
import com.ordera.modules.order.domain.model.Order;
import com.ordera.modules.order.domain.model.OrderItem;
import com.ordera.modules.order.domain.model.OrderItemModifier;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemModifierJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.ordera.modules.order.presentation.dto.response.CartItemDetailResponse;
import com.ordera.modules.order.presentation.dto.response.CartResponse;
import com.ordera.modules.order.presentation.dto.response.CheckoutOrderResponse;
import com.ordera.modules.order.presentation.dto.response.OrderSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderDtoMapper {

    Order toDomain(OrderJpaEntity entity);
    OrderJpaEntity toEntity(Order domain);
    List<Order> toOrderDomainList(List<OrderJpaEntity> entities);

    OrderItem toDomain(OrderItemJpaEntity entity);
    OrderItemJpaEntity toEntity(OrderItem domain);
    List<OrderItem> toItemDomainList(List<OrderItemJpaEntity> entities);

    OrderItemModifier toDomain(OrderItemModifierJpaEntity entity);
    OrderItemModifierJpaEntity toEntity(OrderItemModifier domain);
    List<OrderItemModifier> toModifierDomainList(List<OrderItemModifierJpaEntity> entities);

    @Mapping(target = "orderId", source = "id")
    CheckoutOrderResponse toCheckoutResponse(OrderJpaEntity entity);

    @Mapping(target = "orderId", source = "id")
    OrderSummaryResponse toOrderSummaryResponse(OrderJpaEntity entity);

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "totalItemCount", expression = "java(cart.getItems() != null ? cart.getItems().stream().mapToInt(com.ordera.modules.order.domain.model.CartItem::getQuantity).sum() : 0)")
    @Mapping(target = "currency", constant = "NOK")
    CartResponse toCartResponse(Cart cart);

    @Mapping(target = "itemId", source = "id")
    @Mapping(target = "itemPrice", source = "unitPrice")
    CartItemDetailResponse toCartItemDetailResponse(CartItem item);
}

