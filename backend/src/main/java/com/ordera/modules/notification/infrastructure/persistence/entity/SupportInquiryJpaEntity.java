package com.ordera.modules.notification.infrastructure.persistence.entity;

import com.ordera.common.entity.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "support_inquiries", uniqueConstraints = {
        @UniqueConstraint(name = "uq_support_inquiries_code", columnNames = {"tenant_id", "ticket_code"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SupportInquiryJpaEntity extends BaseTenantEntity {

    @Column(name = "ticket_code", nullable = false, length = 32)
    private String ticketCode;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 32)
    private String phoneNumber;

    @Column(name = "receipt_code", length = 64)
    private String receiptCode;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "bill_amount", precision = 12, scale = 2)
    private BigDecimal billAmount;

    @Column(name = "note", nullable = false)
    private String note;

    @Column(name = "receipt_photo_url")
    private String receiptPhotoUrl;

    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "admin_notes")
    private String adminNotes;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

