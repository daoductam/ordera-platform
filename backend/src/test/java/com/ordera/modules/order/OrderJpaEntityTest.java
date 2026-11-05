package com.ordera.modules.order;

import com.ordera.modules.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order JPA Entity Unit Tests")
class OrderJpaEntityTest {

    @Test
    @DisplayName("should_build_order_jpa_entity_with_expected_defaults_and_values")
    void should_build_order_jpa_entity_with_expected_defaults_and_values() {
        // Arrange
        UUID tenantId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        OrderJpaEntity order = OrderJpaEntity.builder()
                .storeId(storeId)
                .userId(userId)
                .orderCode("ORD-20261105-0891")
                .pickupPersonName("David Nguyen")
                .pickupPersonPhone("+4797362748")
                .subtotalAmount(new BigDecimal("267.00"))
                .discountAmount(new BigDecimal("100.00"))
                .tipAmount(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("177.00"))
                .pointsEarned(170)
                .build();
        order.setTenantId(tenantId);

        // Assert
        assertEquals(tenantId, order.getTenantId());
        assertEquals(storeId, order.getStoreId());
        assertEquals("ORD-20261105-0891", order.getOrderCode());
        assertEquals("COUNTER", order.getPickupMethod());
        assertEquals("ASAP", order.getPickupTimeType());
        assertEquals("PENDING", order.getStatus());
        assertEquals("UNPAID", order.getPaymentStatus());
        assertEquals(new BigDecimal("177.00"), order.getTotalAmount());
        assertEquals(170, order.getPointsEarned());
        assertNotNull(order.getCreatedAt());
    }
}
