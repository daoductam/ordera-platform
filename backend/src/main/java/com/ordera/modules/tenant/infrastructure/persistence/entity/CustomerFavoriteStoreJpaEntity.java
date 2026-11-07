package com.ordera.modules.tenant.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_favorite_stores", uniqueConstraints = {
        @UniqueConstraint(name = "uq_customer_store_favorite", columnNames = {"tenant_id", "user_id", "store_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CustomerFavoriteStoreJpaEntity extends BaseTenantEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

