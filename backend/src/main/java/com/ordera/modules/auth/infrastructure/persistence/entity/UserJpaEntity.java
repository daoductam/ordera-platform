package com.ordera.modules.auth.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uq_users_tenant_phone", columnNames = {"tenant_id", "phone_number"}),
        @UniqueConstraint(name = "uq_users_tenant_email", columnNames = {"tenant_id", "email"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity extends BaseTenantAuditEntity {

    @Column(name = "phone_number", nullable = false, length = 32)
    private String phoneNumber;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false, length = 128)
    private String fullName;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "is_phone_verified", nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "role", nullable = false, length = 32)
    @Builder.Default
    private String role = "CUSTOMER";

    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
