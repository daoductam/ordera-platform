package com.ordera.modules.order.infrastructure.mapper;

import com.ordera.modules.order.domain.model.Order;
import com.ordera.modules.order.domain.model.OrderItem;
import com.ordera.modules.order.domain.model.OrderItemModifier;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemModifierJpaEntity;
import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    Order toDomain(OrderJpaEntity entity);
    OrderJpaEntity toEntity(Order domain);
    List<Order> toOrderDomainList(List<OrderJpaEntity> entities);

    OrderItem toDomain(OrderItemJpaEntity entity);
    OrderItemJpaEntity toEntity(OrderItem domain);
    List<OrderItem> toItemDomainList(List<OrderItemJpaEntity> entities);

    OrderItemModifier toDomain(OrderItemModifierJpaEntity entity);
    OrderItemModifierJpaEntity toEntity(OrderItemModifier domain);
    List<OrderItemModifier> toModifierDomainList(List<OrderItemModifierJpaEntity> entities);
}
