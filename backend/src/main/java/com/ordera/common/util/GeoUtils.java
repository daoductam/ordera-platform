package com.ordera.common.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tiện ích tính toán địa lý và khoảng cách GPS.
 */
@UtilityClass
public class GeoUtils {

    public static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Tính khoảng cách giữa 2 tọa độ GPS theo công thức Haversine.
     *
     * @param lat1 Vĩ độ điểm 1
     * @param lon1 Kinh độ điểm 1
     * @param lat2 Vĩ độ điểm 2
     * @param lon2 Kinh độ điểm 2
     * @return Khoảng cách tính bằng kilomet (km) đã làm tròn 2 chữ số thập phân
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        double distance = EARTH_RADIUS_KM * c;

        return round(distance, 2);
    }

    /**
     * Làm tròn số thực với số chữ số thập phân chỉ định.
     */
    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException("Places must be non-negative");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Kiểm tra tính hợp lệ của cặp tọa độ vĩ độ và kinh độ.
     */
    public static boolean isValidCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        return latitude >= -90.0 && latitude <= 90.0 && longitude >= -180.0 && longitude <= 180.0;
    }
}

