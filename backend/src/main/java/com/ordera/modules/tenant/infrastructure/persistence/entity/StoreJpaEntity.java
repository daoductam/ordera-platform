package com.ordera.modules.tenant.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stores")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreJpaEntity extends BaseTenantAuditEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    @Builder.Default
    private String city = "Oslo";

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "phone_number", length = 32)
    private String phoneNumber;

    @Column(name = "opening_hours", length = 100)
    @Builder.Default
    private String openingHours = "10:00 - 22:00";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "accepts_pickup", nullable = false)
    @Builder.Default
    private boolean acceptsPickup = true;

    @Column(name = "accepts_dinein", nullable = false)
    @Builder.Default
    private boolean acceptsDinein = true;
}
