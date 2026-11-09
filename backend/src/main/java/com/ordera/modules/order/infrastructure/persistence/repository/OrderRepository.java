package com.ordera.modules.order.infrastructure.persistence.repository;

import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.stereotype.Repository;

/**
 * Backward compatibility alias for OrderJpaRepository.
 */
@Repository
public interface OrderRepository extends OrderJpaRepository {
}
