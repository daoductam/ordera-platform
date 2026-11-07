package com.ordera.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GeoUtilsTest {

    @Test
    @DisplayName("should calculate distance between two same points as 0.0")
    void should_calculate_zero_distance_for_same_point() {
        double dist = GeoUtils.calculateDistanceKm(59.9111, 10.7485, 59.9111, 10.7485);
        assertThat(dist).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should calculate distance between Oslo and Bergen accurately")
    void should_calculate_distance_between_cities() {
        // Oslo: 59.9139, 10.7522; Bergen: 60.3913, 5.3221 (~304-306 km)
        double dist = GeoUtils.calculateDistanceKm(59.9139, 10.7522, 60.3913, 5.3221);
        assertThat(dist).isBetween(300.0, 310.0);
    }

    @Test
    @DisplayName("should round numbers accurately to specified decimal places")
    void should_round_numbers_correctly() {
        assertThat(GeoUtils.round(12.3456, 2)).isEqualTo(12.35);
        assertThat(GeoUtils.round(12.3444, 2)).isEqualTo(12.34);
        assertThat(GeoUtils.round(10.0, 1)).isEqualTo(10.0);
        assertThat(GeoUtils.round(Double.NaN, 2)).isNaN();
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when round places is negative")
    void should_throw_when_places_is_negative() {
        assertThatThrownBy(() -> GeoUtils.round(12.34, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Places must be non-negative");
    }

    @Test
    @DisplayName("should validate GPS coordinates correctly")
    void should_validate_coordinates() {
        assertThat(GeoUtils.isValidCoordinates(59.9111, 10.7485)).isTrue();
        assertThat(GeoUtils.isValidCoordinates(-90.0, -180.0)).isTrue();
        assertThat(GeoUtils.isValidCoordinates(90.0, 180.0)).isTrue();
        assertThat(GeoUtils.isValidCoordinates(91.0, 10.0)).isFalse();
        assertThat(GeoUtils.isValidCoordinates(10.0, 181.0)).isFalse();
        assertThat(GeoUtils.isValidCoordinates(null, 10.0)).isFalse();
        assertThat(GeoUtils.isValidCoordinates(10.0, null)).isFalse();
    }
}

