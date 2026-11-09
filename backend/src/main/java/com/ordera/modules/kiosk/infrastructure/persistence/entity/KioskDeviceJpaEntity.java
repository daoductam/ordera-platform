package com.ordera.modules.kiosk.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity ánh xạ bảng `kiosk_devices` trong PostgreSQL.
 * Hỗ trợ phân lập đa thương hiệu (Row-Level Security) qua BaseTenantEntity.
 */
@Entity
@Table(name = "kiosk_devices", uniqueConstraints = {
        @UniqueConstraint(name = "uq_kiosk_devices_tenant_code", columnNames = {"tenant_id", "device_code"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class KioskDeviceJpaEntity extends BaseTenantEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "device_code", nullable = false, length = 64)
    private String deviceCode;

    @Column(name = "device_name", nullable = false, length = 128)
    private String deviceName;

    @Column(name = "printer_status", nullable = false, length = 32)
    @Builder.Default
    private String printerStatus = "OK";

    @Column(name = "card_reader_status", nullable = false, length = 32)
    @Builder.Default
    private String cardReaderStatus = "ONLINE";

    @Column(name = "idle_timeout_seconds", nullable = false)
    @Builder.Default
    private int idleTimeoutSeconds = 60;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

