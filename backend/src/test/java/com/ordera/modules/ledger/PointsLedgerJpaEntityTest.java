package com.ordera.modules.ledger;

import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerId;
import com.ordera.modules.ledger.infrastructure.persistence.entity.PointsLedgerJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PointsLedger JPA Entity Unit Tests")
class PointsLedgerJpaEntityTest {

    @Test
    @DisplayName("should_build_points_ledger_jpa_entity_with_correct_balance_and_composite_id")
    void should_build_points_ledger_jpa_entity_with_correct_balance_and_composite_id() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Instant now = Instant.now();

        // Act
        PointsLedgerJpaEntity ledger = PointsLedgerJpaEntity.builder()
                .id(id)
                .createdAt(now)
                .tenantId(tenantId)
                .memberId(memberId)
                .transactionType("ORDER_EARN")
                .pointsDelta(170)
                .balanceAfter(290)
                .referenceType("ORDERS")
                .referenceId("ORD-20261105-0891")
                .description("Points earned from Linn Sushi Sentrum order")
                .build();

        // Assert
        assertEquals(id, ledger.getId());
        assertEquals(now, ledger.getCreatedAt());
        assertEquals(tenantId, ledger.getTenantId());
        assertEquals(memberId, ledger.getMemberId());
        assertEquals("ORDER_EARN", ledger.getTransactionType());
        assertEquals(170, ledger.getPointsDelta());
        assertEquals(290, ledger.getBalanceAfter());
        assertEquals("ORDERS", ledger.getReferenceType());
        assertEquals("ORD-20261105-0891", ledger.getReferenceId());
    }

    @Test
    @DisplayName("should_support_points_ledger_composite_key_equality")
    void should_support_points_ledger_composite_key_equality() {
        // Arrange
        UUID id = UUID.randomUUID();
        Instant timestamp = Instant.now();

        // Act
        PointsLedgerId id1 = new PointsLedgerId(id, timestamp);
        PointsLedgerId id2 = new PointsLedgerId(id, timestamp);

        // Assert
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
