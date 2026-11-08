package com.ordera.modules.loyalty.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitSupportClaimRequest {

    @NotBlank(message = "Mã hóa đơn không được để trống")
    @Size(max = 64, message = "Mã hóa đơn tối đa 64 ký tự")
    private String receiptCode;

    @NotNull(message = "Ngày thanh toán trên hóa đơn không được để trống")
    private LocalDate orderDate;

    @NotNull(message = "Số tiền hóa đơn không được để trống")
    @DecimalMin(value = "1.00", message = "Số tiền hóa đơn tối thiểu là 1.00")
    private BigDecimal billAmount;

    private UUID storeId;

    private String billPhotoUrl;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}

