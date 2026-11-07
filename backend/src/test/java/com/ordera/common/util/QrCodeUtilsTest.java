package com.ordera.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class QrCodeUtilsTest {

    @Test
    @DisplayName("should extract table number from various token formats")
    void should_extract_table_number() {
        assertThat(QrCodeUtils.extractTableNumber("QR-LINN-SENTRUM-T05")).contains(5);
        assertThat(QrCodeUtils.extractTableNumber("qr_linn_tbl_01")).contains(1);
        assertThat(QrCodeUtils.extractTableNumber("table-12")).contains(12);
        assertThat(QrCodeUtils.extractTableNumber("Table 99")).contains(99);
        assertThat(QrCodeUtils.extractTableNumber("T-03")).contains(3);
    }

    @Test
    @DisplayName("should return empty optional when token has no table pattern")
    void should_return_empty_when_no_pattern() {
        assertThat(QrCodeUtils.extractTableNumber(null)).isEmpty();
        assertThat(QrCodeUtils.extractTableNumber("")).isEmpty();
        assertThat(QrCodeUtils.extractTableNumber("INVALID_TOKEN")).isEmpty();
        assertThat(QrCodeUtils.extractTableNumber("NO_TABLE_HERE")).isEmpty();
    }

    @Test
    @DisplayName("should format table number correctly")
    void should_format_table_number() {
        assertThat(QrCodeUtils.formatTableNumber(5, true)).isEqualTo("Table 05");
        assertThat(QrCodeUtils.formatTableNumber(5, false)).isEqualTo("Table 5");
        assertThat(QrCodeUtils.formatTableNumber(12, true)).isEqualTo("Table 12");
    }

    @Test
    @DisplayName("should normalize hyphen to underscore")
    void should_normalize_hyphen_to_underscore() {
        assertThat(QrCodeUtils.normalizeHyphenToUnderscore("qr-tbl-01")).isEqualTo("qr_tbl_01");
        assertThat(QrCodeUtils.normalizeHyphenToUnderscore("qr_tbl_01")).isEqualTo("qr_tbl_01");
        assertThat(QrCodeUtils.normalizeHyphenToUnderscore(null)).isNull();
    }
}

