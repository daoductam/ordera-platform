package com.ordera.modules.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendRegisterOtpRequest {

    @NotBlank(message = "Kênh gửi mã (SMS hoặc EMAIL) không được để trống")
    @Pattern(regexp = "^(SMS|EMAIL)$", message = "Kênh gửi mã phải là SMS hoặc EMAIL")
    private String channel;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Số điện thoại phải tuân thủ định dạng quốc tế E.164")
    private String phoneNumber;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Địa chỉ email không đúng định dạng")
    private String email;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 128, message = "Họ và tên phải từ 2 đến 128 ký tự")
    private String fullName;

    private LocalDate dateOfBirth;
}

