package com.ordera.modules.order.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

/**
 * Backward compatibility alias for OrderItemModifierJpaRepository.
 */
@Repository
public interface OrderItemModifierRepository extends OrderItemModifierJpaRepository {
}
