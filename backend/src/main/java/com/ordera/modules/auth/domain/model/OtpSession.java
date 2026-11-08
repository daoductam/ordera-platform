package com.ordera.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Value Object biểu diễn phiên xác thực OTP tạm thời.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSession {

    private UUID tenantId;
    private String recipient;
    private OtpChannel channel;
    private OtpPurpose purpose;
    private String otpHash;
    private int attemptCount;
    private boolean verified;
    private String ipAddress;
    private Instant expiresAt;
    private Instant createdAt;
}
