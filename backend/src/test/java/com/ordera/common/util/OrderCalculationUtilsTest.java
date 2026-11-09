package com.ordera.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("OrderCalculationUtils Financial Math Unit Tests")
class OrderCalculationUtilsTest {

    @Test
    @DisplayName("should_calculate_item_total_with_modifiers_accurately")
    void should_calculate_item_total_with_modifiers_accurately() {
        // Arrange: 89.00 NOK base price + 15.00 (size L) + 10.00 (popping), quantity 2
        BigDecimal unitPrice = new BigDecimal("89.00");
        List<BigDecimal> modifierPrices = List.of(new BigDecimal("15.00"), new BigDecimal("10.00"));
        int quantity = 2;

        // Act
        BigDecimal total = OrderCalculationUtils.calculateItemTotal(unitPrice, modifierPrices, quantity);

        // Assert: (89 + 15 + 10) * 2 = 114 * 2 = 228.00 NOK
        assertEquals(new BigDecimal("228.00"), total);
    }

    @Test
    @DisplayName("should_calculate_subtotal_from_multiple_items")
    void should_calculate_subtotal_from_multiple_items() {
        // Arrange
        List<BigDecimal> items = List.of(
                new BigDecimal("178.00"),
                new BigDecimal("72.00")
        );

        // Act
        BigDecimal subtotal = OrderCalculationUtils.calculateSubtotal(items);

        // Assert: 178.00 + 72.00 = 250.00 NOK
        assertEquals(new BigDecimal("250.00"), subtotal);
    }

    @Test
    @DisplayName("should_calculate_vat_15_percent_accurately")
    void should_calculate_vat_15_percent_accurately() {
        // Arrange: Total amount 185.00 NOK -> 185.00 * 15 / 115 = 24.1304... -> 24.13 NOK
        BigDecimal amount = new BigDecimal("185.00");

        // Act
        BigDecimal vat = OrderCalculationUtils.calculateVat(amount);

        // Assert
        assertEquals(new BigDecimal("24.13"), vat);

        // Arrange 2: Net 175.00 NOK (250 subtotal - 75 discount) -> 175.00 * 15 / 115 = 22.826... -> 22.83 NOK
        BigDecimal netAmount = new BigDecimal("175.00");
        BigDecimal netVat = OrderCalculationUtils.calculateVat(netAmount);
        assertEquals(new BigDecimal("22.83"), netVat);
    }

    @Test
    @DisplayName("should_calculate_total_with_discount_and_null_safe_tip")
    void should_calculate_total_with_discount_and_null_safe_tip() {
        // Case 1: Subtotal 250.00, Discount 75.00, Tip 10.00 -> 185.00 NOK
        BigDecimal subtotal = new BigDecimal("250.00");
        BigDecimal discount = new BigDecimal("75.00");
        BigDecimal tip = new BigDecimal("10.00");

        BigDecimal total = OrderCalculationUtils.calculateTotal(subtotal, discount, tip);
        assertEquals(new BigDecimal("185.00"), total);

        // Case 2: Null tip should default to 0.00
        BigDecimal totalNoTip = OrderCalculationUtils.calculateTotal(subtotal, discount, null);
        assertEquals(new BigDecimal("175.00"), totalNoTip);
    }

    @Test
    @DisplayName("should_calculate_points_to_earn_excluding_tip")
    void should_calculate_points_to_earn_excluding_tip() {
        // Arrange: Subtotal 250.00, Discount 80.00, TierRate 1.0 (Tip is not in calculation)
        // points = floor((250.00 - 80.00) * 1.0) = 170 points
        BigDecimal subtotal = new BigDecimal("250.00");
        BigDecimal discount = new BigDecimal("80.00");
        BigDecimal tierRate = new BigDecimal("1.0");

        // Act
        int points = OrderCalculationUtils.calculatePointsToEarn(subtotal, discount, tierRate);

        // Assert
        assertEquals(170, points);

        // Arrange 2: Tier rate 1.5 (Gold Tier) -> floor(170 * 1.5) = 255
        BigDecimal goldRate = new BigDecimal("1.5");
        int goldPoints = OrderCalculationUtils.calculatePointsToEarn(subtotal, discount, goldRate);
        assertEquals(255, goldPoints);
    }
}

