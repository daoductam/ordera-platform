package com.ordera.modules.loyalty.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "tier_definitions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_tier_tenant_name", columnNames = {"tenant_id", "tier_name"}),
        @UniqueConstraint(name = "uq_tier_tenant_order", columnNames = {"tenant_id", "tier_order"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TierDefinitionJpaEntity extends BaseTenantEntity {

    @Column(name = "tier_name", nullable = false, length = 32)
    private String tierName;

    @Column(name = "tier_order", nullable = false)
    private int tierOrder;

    @Column(name = "min_points_required", nullable = false)
    @Builder.Default
    private int minPointsRequired = 0;

    @Column(name = "theme_color_hex", nullable = false, length = 16)
    private String themeColorHex;

    @Column(name = "earn_points_rate", nullable = false, precision = 6, scale = 4)
    @Builder.Default
    private BigDecimal earnPointsRate = new BigDecimal("1.0000");

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "benefits_summary", columnDefinition = "jsonb")
    private List<String> benefitsSummary;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
