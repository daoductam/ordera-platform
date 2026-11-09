package com.ordera.modules.notification.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO yêu cầu gửi biểu mẫu khiếu nại đối soát điểm thiếu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitInquiryRequest {

    @NotBlank(message = "Email liên hệ không được để trống")
    @Email(message = "Email không đúng định dạng chuẩn")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+?[0-9]{8,15})$", message = "Số điện thoại phải từ 8 đến 15 chữ số")
    private String phoneNumber;

    @Size(max = 64, message = "Mã biên lai không được vượt quá 64 ký tự")
    private String receiptCode;

    private LocalDate orderDate;

    @DecimalMin(value = "0.00", message = "Số tiền hóa đơn phải lớn hơn hoặc bằng 0")
    private BigDecimal billAmount;

    @NotBlank(message = "Nội dung cần hỗ trợ không được để trống")
    @Size(min = 5, max = 500, message = "Nội dung cần hỗ trợ phải từ 5 đến 500 ký tự")
    private String note;

    private String receiptPhotoUrl;
}

