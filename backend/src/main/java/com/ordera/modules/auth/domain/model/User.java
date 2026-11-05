package com.ordera.modules.auth.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private UUID tenantId;
    private String phoneNumber;
    private String email;
    private String fullName;
    private String passwordHash;
    private boolean phoneVerified;
    private boolean emailVerified;
    private String role;
    private String status;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
}
