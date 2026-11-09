package com.ordera.modules.order.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CheckoutOrderRequest {

    @NotNull(message = "cartId must not be null")
    private UUID cartId;

    @NotBlank(message = "pickupMethod must not be blank")
    private String pickupMethod;

    @Size(max = 32, message = "tableNumber must not exceed 32 characters")
    private String tableNumber;

    @NotBlank(message = "pickupPersonName must not be blank")
    @Size(max = 128, message = "pickupPersonName must not exceed 128 characters")
    private String pickupPersonName;

    @NotBlank(message = "pickupPersonPhone must not be blank")
    @Size(max = 32, message = "pickupPersonPhone must not exceed 32 characters")
    private String pickupPersonPhone;

    private String voucherCode;

    private UUID rewardGiftId;

    @DecimalMin(value = "0.00", message = "tipAmount must not be negative")
    @Builder.Default
    private BigDecimal tipAmount = BigDecimal.ZERO;

    @NotBlank(message = "paymentMethod must not be blank")
    private String paymentMethod;

    private String eReceiptEmail;
}
