package com.ordera.modules.order.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewCheckoutRequest {

    @NotNull(message = "cartId must not be null")
    private UUID cartId;

    private String voucherCode;

    private UUID rewardGiftId;

    @DecimalMin(value = "0.00", message = "tipAmount must not be negative")
    @Builder.Default
    private BigDecimal tipAmount = BigDecimal.ZERO;

    private String pickupMethod;
}
