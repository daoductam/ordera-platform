package com.ordera.modules.kiosk.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskCheckoutResponse {

    private String orderId;
    private String orderCode;
    private String queueNumber;
    private String status;
    private BigDecimal subtotalAmount;
    private BigDecimal tipAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private String currency;
    private int pointsEarned;
    private boolean printCommandDispatched;
    private int resetTimeoutSeconds;
}

