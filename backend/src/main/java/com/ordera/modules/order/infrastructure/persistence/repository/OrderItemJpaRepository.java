package com.ordera.modules.order.infrastructure.persistence.repository;

import com.ordera.modules.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, UUID> {

    List<OrderItemJpaEntity> findByOrderId(UUID orderId);
}
