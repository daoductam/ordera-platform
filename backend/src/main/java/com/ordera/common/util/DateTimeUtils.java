package com.ordera.common.util;

import lombok.experimental.UtilityClass;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích xử lý thời gian, khung giờ mở cửa và sinh slot đặt hẹn.
 */
@UtilityClass
public class DateTimeUtils {

    private static final Pattern OPENING_HOURS_PATTERN = Pattern.compile("(\\d{1,2}:\\d{2})\\s*-\\s*(\\d{1,2}:\\d{2})");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static final List<String> DEFAULT_SLOTS = List.of(
            "11:30 - 12:00",
            "12:00 - 12:30",
            "12:30 - 13:00",
            "17:30 - 18:00",
            "18:00 - 18:30"
    );

    /**
     * Sinh danh sách các khung giờ 30 phút dựa trên chuỗi giờ mở cửa (VD: "10:00 - 22:00").
     *
     * @param openingHours Chuỗi giờ mở cửa dạng "HH:mm - HH:mm"
     * @return Danh sách khung giờ 30 phút, hoặc DEFAULT_SLOTS nếu không phân tích được
     */
    public static List<String> generateTimeSlots(String openingHours) {
        if (openingHours == null || openingHours.isBlank()) {
            return DEFAULT_SLOTS;
        }

        Matcher matcher = OPENING_HOURS_PATTERN.matcher(openingHours.trim());
        if (!matcher.find()) {
            return DEFAULT_SLOTS;
        }

        try {
            LocalTime start = LocalTime.parse(matcher.group(1), TIME_FORMATTER);
            LocalTime end = LocalTime.parse(matcher.group(2), TIME_FORMATTER);

            if (!end.isAfter(start)) {
                return DEFAULT_SLOTS;
            }

            List<String> slots = new ArrayList<>();
            LocalTime current = start;
            while (current.plusMinutes(30).isBefore(end) || current.plusMinutes(30).equals(end)) {
                LocalTime next = current.plusMinutes(30);
                slots.add(String.format("%s - %s", current.format(DISPLAY_FORMATTER), next.format(DISPLAY_FORMATTER)));
                current = next;
            }

            return slots.isEmpty() ? DEFAULT_SLOTS : slots;
        } catch (Exception e) {
            return DEFAULT_SLOTS;
        }
    }
}

