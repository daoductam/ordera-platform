package com.ordera.common.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Tiện ích sinh mã xác thực OTP, băm bảo mật SHA-256 và làm mờ số điện thoại / email.
 */
@UtilityClass
public class OtpUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Sinh mã OTP gồm các chữ số ngẫu nhiên an toàn.
     *
     * @param length Độ dài chuỗi số (ví dụ: 6)
     * @return Chuỗi chữ số OTP
     */
    public static String generateNumericOtp(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be greater than 0");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Băm chuỗi văn bản bằng thuật toán SHA-256 (cho audit log không lưu plain text).
     *
     * @param raw Chuỗi gốc cần băm
     * @return Chuỗi hex 64 ký tự thường, hoặc rỗng nếu raw null/empty
     */
    public static String sha256(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Che bớt số điện thoại theo định dạng chuẩn hiển thị (** ***748).
     *
     * @param phoneNumber Số điện thoại quốc tế (+4797362748)
     * @return Chuỗi số đã làm mờ
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        String trimmed = phoneNumber.trim();
        if (trimmed.length() <= 3) {
            return trimmed;
        }

        String last3 = trimmed.substring(trimmed.length() - 3);
        return "** ***" + last3;
    }

    /**
     * Làm mờ địa chỉ email để bảo vệ quyền riêng tư (abytran@ordera.no -> a***n@ordera.no).
     *
     * @param email Địa chỉ email
     * @return Chuỗi email đã làm mờ
     */
    public static String maskEmail(String email) {
        if (email == null) {
            return "";
        }
        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0 || atIndex == trimmed.length() - 1) {
            return trimmed;
        }

        String username = trimmed.substring(0, atIndex);
        String domain = trimmed.substring(atIndex + 1);

        if (username.length() == 1) {
            return username + "@" + domain;
        } else if (username.length() == 2) {
            return username.charAt(0) + "*@" + domain;
        } else {
            return username.charAt(0) + "***" + username.charAt(username.length() - 1) + "@" + domain;
        }
    }
}

