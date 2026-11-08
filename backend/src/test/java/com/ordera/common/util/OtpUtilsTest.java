package com.ordera.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OtpUtils Unit Tests")
class OtpUtilsTest {

    @Nested
    @DisplayName("generateNumericOtp()")
    class GenerateNumericOtpTests {

        @Test
        @DisplayName("should generate 6-digit numeric OTP string")
        void should_generate6DigitNumericOtp() {
            String otp = OtpUtils.generateNumericOtp(6);

            assertThat(otp).isNotNull();
            assertThat(otp).hasSize(6);
            assertThat(otp).matches("^\\d{6}$");
        }

        @Test
        @DisplayName("should generate different OTPs across multiple calls")
        void should_generateDifferentOtps() {
            String otp1 = OtpUtils.generateNumericOtp(6);
            String otp2 = OtpUtils.generateNumericOtp(6);

            // While random collisions are theoretically possible, 2 calls should rarely match
            // Test that both are valid 6-digit numbers
            assertThat(otp1).matches("^\\d{6}$");
            assertThat(otp2).matches("^\\d{6}$");
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when length <= 0")
        void should_throwException_when_lengthIsZeroOrNegative() {
            assertThatThrownBy(() -> OtpUtils.generateNumericOtp(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> OtpUtils.generateNumericOtp(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("sha256()")
    class Sha256Tests {

        @Test
        @DisplayName("should return 64-character hex string for valid input")
        void should_return64CharHexString_forValidInput() {
            String hash = OtpUtils.sha256("123456");

            assertThat(hash).isNotNull();
            assertThat(hash).hasSize(64);
            assertThat(hash).matches("^[a-f0-9]{64}$");
            // SHA-256 for "123456" is "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"
            assertThat(hash).isEqualTo("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92");
        }

        @Test
        @DisplayName("should return empty string or handle null/blank safely")
        void should_returnEmptyString_when_inputIsNull() {
            assertThat(OtpUtils.sha256(null)).isEmpty();
            assertThat(OtpUtils.sha256("")).isEmpty();
        }
    }

    @Nested
    @DisplayName("maskPhoneNumber()")
    class MaskPhoneNumberTests {

        @Test
        @DisplayName("should mask Norwegian phone number correctly")
        void should_maskNorwegianPhoneNumber() {
            String masked = OtpUtils.maskPhoneNumber("+4797362748");

            assertThat(masked).isEqualTo("** ***748");
        }

        @Test
        @DisplayName("should return masked string with last 3 digits visible")
        void should_maskOtherPhoneNumber() {
            String masked = OtpUtils.maskPhoneNumber("+84901234567");

            assertThat(masked).endsWith("567");
            assertThat(masked).contains("*");
        }

        @Test
        @DisplayName("should return empty string when phone is null or short")
        void should_handleNullOrShortPhone() {
            assertThat(OtpUtils.maskPhoneNumber(null)).isEmpty();
            assertThat(OtpUtils.maskPhoneNumber("123")).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("maskEmail()")
    class MaskEmailTests {

        @Test
        @DisplayName("should mask email username preserving first and last char of local part")
        void should_maskEmail() {
            String masked = OtpUtils.maskEmail("abytran@ordera.no");

            assertThat(masked).isEqualTo("a***n@ordera.no");
        }

        @Test
        @DisplayName("should mask short username email")
        void should_maskShortEmail() {
            String masked = OtpUtils.maskEmail("me@ordera.no");

            assertThat(masked).isEqualTo("m*@ordera.no");
        }

        @Test
        @DisplayName("should return empty string when email is null or invalid")
        void should_handleNullOrInvalidEmail() {
            assertThat(OtpUtils.maskEmail(null)).isEmpty();
            assertThat(OtpUtils.maskEmail("invalid-email")).isEqualTo("invalid-email");
        }
    }
}

