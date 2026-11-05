package com.ordera.modules.reward.infrastructure.persistence.entity;

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
@Table(name = "user_gift_redemptions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserGiftRedemptionJpaEntity extends BaseTenantEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "gift_id", nullable = false)
    private UUID giftId;

    @Column(name = "points_spent", nullable = false)
    private int pointsSpent;

    @Column(name = "redemption_code", nullable = false, unique = true, length = 32)
    private String redemptionCode;

    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

