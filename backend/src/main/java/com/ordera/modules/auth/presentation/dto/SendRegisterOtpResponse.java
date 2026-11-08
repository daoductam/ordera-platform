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
public class SendRegisterOtpResponse {

    private String channel;
    private String recipientMasked;
    private long cooldownSeconds;
}
