package com.ordera.modules.kiosk.presentation.dto.request;

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
public class KioskQuickEarnRequest {

    @NotNull(message = "storeId không được để trống")
    private UUID storeId;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @NotNull(message = "Số tiền hóa đơn không được để trống")
    @DecimalMin(value = "1.00", message = "Số tiền hóa đơn tích điểm tối thiểu phải từ 1.00 NOK trở lên")
    private BigDecimal billAmount;

    @Size(max = 64, message = "Mã hóa đơn POS không được vượt quá 64 ký tự")
    private String externalReceiptRef;
}

