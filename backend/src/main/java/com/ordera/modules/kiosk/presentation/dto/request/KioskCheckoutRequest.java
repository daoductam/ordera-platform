package com.ordera.modules.kiosk.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskCheckoutRequest {

    @NotNull(message = "storeId không được để trống")
    private UUID storeId;

    @NotBlank(message = "kioskDeviceId không được để trống")
    private String kioskDeviceId;

    @NotBlank(message = "customerPhone không được để trống")
    private String customerPhone;

    private String customerName;

    @DecimalMin(value = "0.00", message = "Tiền tip không được âm")
    @Builder.Default
    private BigDecimal tipAmount = BigDecimal.ZERO;

    @NotBlank(message = "paymentMethod không được để trống")
    private String paymentMethod;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    @Valid
    private List<KioskOrderItemRequest> items;
}

