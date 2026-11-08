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
public class SendOtpRequest {

    @NotBlank(message = "Kênh gửi mã (channel) không được để trống")
    @Builder.Default
    private String channel = "SMS";

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Số điện thoại phải tuân thủ định dạng quốc tế E.164 (bắt đầu bằng dấu +, ví dụ: +4797362748)")
    private String phoneNumber;
}

