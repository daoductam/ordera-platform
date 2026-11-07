package com.ordera.common.util;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích xử lý và trích xuất thông tin mã QR bàn ăn và thiết bị.
 */
@UtilityClass
public class QrCodeUtils {

    /**
     * Regex trích xuất số bàn ăn từ chuỗi token QR (ví dụ: QR-LINN-SENTRUM-T05, table-5, tbl_02).
     */
    public static final Pattern TABLE_NUM_PATTERN = Pattern.compile("(?i)(?:t|tbl|table)[-_ ]*(\\d+)");

    /**
     * Trích xuất số thứ tự bàn từ mã token QR.
     *
     * @param token Chuỗi token thô
     * @return Optional chứa số bàn ăn nếu trích xuất thành công
     */
    public static Optional<Integer> extractTableNumber(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = TABLE_NUM_PATTERN.matcher(token.trim());
        if (matcher.find()) {
            try {
                return Optional.of(Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Định dạng tên hiển thị của bàn ăn.
     *
     * @param number Số thứ tự bàn
     * @param zeroPadded Có đệm số 0 ở đầu hay không (true -> "Table 05", false -> "Table 5")
     */
    public static String formatTableNumber(int number, boolean zeroPadded) {
        if (zeroPadded) {
            return String.format("Table %02d", number);
        }
        return "Table " + number;
    }

    /**
     * Chuẩn hóa dấu gạch nối sang dấu gạch dưới (VD: qr-tbl-01 -> qr_tbl_01).
     */
    public static String normalizeHyphenToUnderscore(String token) {
        if (token == null) {
            return null;
        }
        return token.replace("-", "_");
    }
}

