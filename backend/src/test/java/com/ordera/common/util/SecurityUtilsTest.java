package com.ordera.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityUtilsTest {

    @Test
    @DisplayName("should parse valid UUID string correctly")
    void should_parse_valid_uuid() {
        UUID expected = UUID.randomUUID();
        assertThat(SecurityUtils.parseUuid(expected.toString())).contains(expected);
    }

    @Test
    @DisplayName("should return empty optional for invalid or null UUID string")
    void should_return_empty_for_invalid_uuid() {
        assertThat(SecurityUtils.parseUuid(null)).isEmpty();
        assertThat(SecurityUtils.parseUuid("")).isEmpty();
        assertThat(SecurityUtils.parseUuid("invalid-uuid-string")).isEmpty();
    }

    @Test
    @DisplayName("should resolve user id from header when present and valid")
    void should_resolve_user_id_from_header() {
        UUID expected = UUID.randomUUID();
        UUID actual = SecurityUtils.resolveCurrentUserId(expected.toString(), null);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("should resolve user id from principal when header is absent")
    void should_resolve_user_id_from_principal() {
        UUID expected = UUID.randomUUID();
        Principal principal = expected::toString;
        UUID actual = SecurityUtils.resolveCurrentUserId(null, principal);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("should return null when both header and principal are absent or invalid")
    void should_return_null_when_unauthenticated() {
        assertThat(SecurityUtils.resolveCurrentUserId(null, null)).isNull();
        assertThat(SecurityUtils.resolveCurrentUserId("invalid", () -> "not-a-uuid")).isNull();
    }
}

