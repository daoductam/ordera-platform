package com.ordera.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeUtilsTest {

    @Test
    @DisplayName("should generate 30-min slots based on valid opening hours")
    void should_generate_slots_from_opening_hours() {
        List<String> slots = DateTimeUtils.generateTimeSlots("10:00 - 12:00");
        assertThat(slots).containsExactly(
                "10:00 - 10:30",
                "10:30 - 11:00",
                "11:00 - 11:30",
                "11:30 - 12:00"
        );
    }

    @Test
    @DisplayName("should return default slots when opening hours is null or invalid")
    void should_return_default_slots_on_invalid_input() {
        assertThat(DateTimeUtils.generateTimeSlots(null)).isEqualTo(DateTimeUtils.DEFAULT_SLOTS);
        assertThat(DateTimeUtils.generateTimeSlots("")).isEqualTo(DateTimeUtils.DEFAULT_SLOTS);
        assertThat(DateTimeUtils.generateTimeSlots("invalid-hours")).isEqualTo(DateTimeUtils.DEFAULT_SLOTS);
        assertThat(DateTimeUtils.generateTimeSlots("22:00 - 10:00")).isEqualTo(DateTimeUtils.DEFAULT_SLOTS);
    }
}

