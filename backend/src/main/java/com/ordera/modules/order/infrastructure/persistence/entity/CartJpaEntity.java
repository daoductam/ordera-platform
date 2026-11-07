package com.ordera.modules.order.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "carts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartJpaEntity extends BaseTenantAuditEntity {

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "comment", length = 200)
    private String comment;

    @Column(name = "applied_voucher_code", length = 64)
    private String appliedVoucherCode;

    @Column(name = "applied_gift_id")
    private UUID appliedGiftId;
}

