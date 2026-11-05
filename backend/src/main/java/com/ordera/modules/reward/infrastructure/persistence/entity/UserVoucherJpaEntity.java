package com.ordera.modules.reward.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_vouchers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucherJpaEntity extends BaseTenantEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "voucher_id", nullable = false)
    private UUID voucherId;

    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private String status = "AVAILABLE";

    @Column(name = "applied_order_id")
    private UUID appliedOrderId;

    @Column(name = "redeemed_at", nullable = false)
    @Builder.Default
    private Instant redeemedAt = Instant.now();

    @Column(name = "used_at")
    private Instant usedAt;
}
