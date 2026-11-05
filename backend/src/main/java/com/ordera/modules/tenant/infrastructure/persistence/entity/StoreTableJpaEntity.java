package com.ordera.modules.tenant.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "store_tables")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StoreTableJpaEntity extends BaseTenantEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "table_number", nullable = false, length = 32)
    private String tableNumber;

    @Column(name = "seating_capacity", nullable = false)
    @Builder.Default
    private int seatingCapacity = 4;

    @Column(name = "qr_code_token", unique = true, length = 128)
    private String qrCodeToken;

    @Column(name = "is_occupied", nullable = false)
    @Builder.Default
    private boolean occupied = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
