package com.ordera.modules.auth.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpAuditLog {

    private UUID id;
    private UUID tenantId;
    private String channel;
    private String recipient;
    private String otpHash;
    private String purpose;
    private int attemptCount;
    private boolean verified;
    private String ipAddress;
    private Instant expiresAt;
    private Instant createdAt;
}
