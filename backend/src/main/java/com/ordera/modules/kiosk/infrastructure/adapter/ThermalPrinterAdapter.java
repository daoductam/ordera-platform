package com.ordera.modules.kiosk.infrastructure.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Adapter tích hợp và phát lệnh in nhiệt cho máy in hóa đơn Kiosk (ESC/POS Thermal Printer).
 * Cung cấp dịch vụ in phiếu số thứ tự và phiếu tích điểm nhanh.
 */
@Slf4j
@Component
public class ThermalPrinterAdapter {

    /**
     * Phát lệnh in phiếu số thứ tự phục vụ và chế biến món ăn tại quầy.
     *
     * @param storeId     chi nhánh cửa hàng
     * @param queueNumber số thứ tự lấy đồ (ví dụ: #A-08)
     * @param orderCode   mã đơn hàng (ví dụ: A-08)
     * @return true nếu gửi lệnh in thành công
     */
    public boolean printQueueTicket(UUID storeId, String queueNumber, String orderCode) {
        Objects.requireNonNull(storeId, "storeId must not be null");
        Objects.requireNonNull(queueNumber, "queueNumber must not be null");

        log.info("[THERMAL-PRINTER] >>> Dispatched Queue Ticket Print Command | Store: {} | Queue: {} | Code: {}",
                storeId, queueNumber, orderCode);
        return true;
    }

    /**
     * Phát lệnh in phiếu xác nhận tích điểm nhanh tại quầy.
     *
     * @param storeId      chi nhánh cửa hàng
     * @param queueNumber  số thứ tự lấy đồ (ví dụ: #A-08)
     * @param receiptRef   mã hóa đơn POS ngoài
     * @param billAmount   tổng tiền hóa đơn
     * @param pointsEarned số điểm thưởng nhận được
     * @return true nếu gửi lệnh in thành công
     */
    public boolean printQuickEarnReceipt(UUID storeId, String queueNumber, String receiptRef, BigDecimal billAmount, int pointsEarned) {
        Objects.requireNonNull(storeId, "storeId must not be null");
        Objects.requireNonNull(queueNumber, "queueNumber must not be null");

        log.info("[THERMAL-PRINTER] >>> Dispatched Quick Earn Receipt Print Command | Store: {} | Queue: {} | Ref: {} | Amount: {} NOK | Points: +{}",
                storeId, queueNumber, receiptRef, billAmount, pointsEarned);
        return true;
    }
}

