package com.ordera.modules.order.infrastructure.persistence.repository;

import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemModifierJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemModifierJpaRepository extends JpaRepository<OrderItemModifierJpaEntity, UUID> {

    List<OrderItemModifierJpaEntity> findByOrderItemId(UUID orderItemId);

    List<OrderItemModifierJpaEntity> findByOrderItemIdIn(List<UUID> orderItemIds);
}

