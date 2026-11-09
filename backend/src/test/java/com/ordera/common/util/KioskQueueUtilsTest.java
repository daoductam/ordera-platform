package com.ordera.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KioskQueueUtils Unit Tests")
class KioskQueueUtilsTest {

    @Test
    @DisplayName("should_format_queue_number_to_two_digits_with_prefix")
    void should_format_queue_number_to_two_digits_with_prefix() {
        assertEquals("#A-01", KioskQueueUtils.formatQueueNumber(1));
        assertEquals("#A-08", KioskQueueUtils.formatQueueNumber(8));
        assertEquals("#A-15", KioskQueueUtils.formatQueueNumber(15));
        assertEquals("#A-99", KioskQueueUtils.formatQueueNumber(99));
    }

    @Test
    @DisplayName("should_cycle_queue_numbers_when_exceeding_max")
    void should_cycle_queue_numbers_when_exceeding_max() {
        assertEquals("#A-01", KioskQueueUtils.formatQueueNumber(100));
        assertEquals("#A-02", KioskQueueUtils.formatQueueNumber(101));
    }

    @Test
    @DisplayName("should_strip_hash_to_produce_order_code")
    void should_strip_hash_to_produce_order_code() {
        assertEquals("A-08", KioskQueueUtils.toOrderCode("#A-08"));
        assertEquals("A-01", KioskQueueUtils.toOrderCode(null));
    }
}

