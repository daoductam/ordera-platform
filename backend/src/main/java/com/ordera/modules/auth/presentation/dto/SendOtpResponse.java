package com.ordera.modules.auth.presentation.dto;

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
public class SendOtpResponse {

    private String phoneNumber;
    private String maskedPhoneNumber;
    private String channel;
    private long cooldownSeconds;
    private long resendAllowedAfterSeconds;
}
