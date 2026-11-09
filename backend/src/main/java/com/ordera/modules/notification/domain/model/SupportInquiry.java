package com.ordera.modules.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain model đại diện cho phiếu khiếu nại đối soát điểm cũ bị thất lạc.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportInquiry {

    private UUID id;
    private UUID tenantId;
    private String ticketCode;
    private UUID userId;
    private String email;
    private String phoneNumber;
    private String receiptCode;
    private LocalDate orderDate;
    private BigDecimal billAmount;
    private String note;
    private String receiptPhotoUrl;
    private InquiryStatus status;
    private String adminNotes;
    private Instant resolvedAt;
    private Instant createdAt;

    public void resolve(String notes, Instant now) {
        this.status = InquiryStatus.RESOLVED;
        this.adminNotes = notes;
        this.resolvedAt = now;
    }

    public void reject(String notes, Instant now) {
        this.status = InquiryStatus.REJECTED;
        this.adminNotes = notes;
        this.resolvedAt = now;
    }
}

