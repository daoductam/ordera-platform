package com.ordera.common.util;

import com.ordera.modules.order.domain.constant.OrderConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Tiện ích tính toán tài chính đơn hàng (Order Financial Calculator).
 * Đảm bảo độ chính xác số học tuyệt đối với BigDecimal (scale = 2, RoundingMode.HALF_UP).
 * Tuyệt đối không sử dụng số thực float/double trong tính toán tiền tệ.
 */
public final class OrderCalculationUtils {

    private OrderCalculationUtils() {
    }

    /**
     * Tính thành tiền của một dòng món ăn kèm modifiers:
     * itemTotal = (unitPrice + sum(extraPrice)) * quantity
     */
    public static BigDecimal calculateItemTotal(BigDecimal unitPrice, List<BigDecimal> modifierPrices, int quantity) {
        BigDecimal base = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        BigDecimal extras = BigDecimal.ZERO;
        if (modifierPrices != null) {
            for (BigDecimal p : modifierPrices) {
                if (p != null) {
                    extras = extras.add(p);
                }
            }
        }
        BigDecimal unitTotal = base.add(extras);
        return unitTotal.multiply(BigDecimal.valueOf(quantity)).setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Tính tổng phụ Subtotal Amount từ danh sách thành tiền của các món.
     */
    public static BigDecimal calculateSubtotal(List<BigDecimal> itemTotals) {
        if (itemTotals == null || itemTotals.isEmpty()) {
            return BigDecimal.ZERO.setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal subtotal = BigDecimal.ZERO;
        for (BigDecimal total : itemTotals) {
            if (total != null) {
                subtotal = subtotal.add(total);
            }
        }
        return subtotal.setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Tính thuế GTGT VAT 15% theo công thức chuẩn: Total * 15 / 115 (hoặc TaxableAmount * 15 / 115).
     * Thuế VAT đã bao gồm trong giá niêm yết (Gross Amount).
     */
    public static BigDecimal calculateVat(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP);
        }
        return amount.multiply(OrderConstants.VAT_RATE)
                .divide(OrderConstants.VAT_DIVISOR, OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Tính tổng số tiền thanh toán thực tế (Final Total Amount):
     * total = max(0, subtotal - discount) + safeTip
     * Tiền tip luôn được kiểm tra null-safety.
     */
    public static BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal discount, BigDecimal tip) {
        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal safeDiscount = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal safeTip = tip != null ? tip : BigDecimal.ZERO;

        BigDecimal discounted = safeSubtotal.subtract(safeDiscount);
        if (discounted.compareTo(BigDecimal.ZERO) < 0) {
            discounted = BigDecimal.ZERO;
        }
        return discounted.add(safeTip).setScale(OrderConstants.DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Tính số điểm thưởng dự kiến tích lũy từ đơn hàng:
     * pointsToEarn = floor((subtotal - discount) * tierRate)
     * Tiền tip hoàn toàn KHÔNG được tính vào điểm thưởng.
     */
    public static int calculatePointsToEarn(BigDecimal subtotal, BigDecimal discount, BigDecimal tierRate) {
        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal safeDiscount = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal safeRate = tierRate != null && tierRate.compareTo(BigDecimal.ZERO) > 0 ? tierRate : BigDecimal.ONE;

        BigDecimal eligibleAmount = safeSubtotal.subtract(safeDiscount);
        if (eligibleAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal calculatedPoints = eligibleAmount.multiply(safeRate);
        return calculatedPoints.setScale(0, RoundingMode.FLOOR).intValue();
    }
}

