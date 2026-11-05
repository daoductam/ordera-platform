package com.ordera.modules.order.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item_modifiers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemModifierJpaEntity extends BaseEntity {

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "modifier_group_name", nullable = false, length = 100)
    private String modifierGroupName;

    @Column(name = "modifier_name", nullable = false, length = 100)
    private String modifierName;

    @Column(name = "extra_price", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal extraPrice = BigDecimal.ZERO;
}
