package com.ordera.modules.order.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

/**
 * Backward compatibility alias for OrderItemJpaRepository.
 */
@Repository
public interface OrderItemRepository extends OrderItemJpaRepository {
}
