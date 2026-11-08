package com.ordera.modules.auth.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Số điện thoại phải tuân thủ định dạng quốc tế E.164")
    private String phoneNumber;

    @NotBlank(message = "Mã xác thực OTP không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Mã xác thực OTP phải gồm đúng 6 chữ số")
    private String otpCode;
}
