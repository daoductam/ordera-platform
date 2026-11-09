package com.ordera.modules.kiosk.domain.model;

/**
 * Trạng thái phần cứng thiết bị Kiosk POS (Máy in nhiệt, máy quẹt thẻ, kết nối trạm).
 */
public enum HardwareStatus {
    ONLINE,
    OFFLINE,
    OUT_OF_PAPER,
    DECLINED,
    OK,
    ERROR
}

