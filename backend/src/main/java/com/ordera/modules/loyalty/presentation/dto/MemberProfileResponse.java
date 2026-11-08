package com.ordera.modules.loyalty.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private LocalDate dateOfBirth;
    private String gender;
    private String avatarUrl;
    private boolean phoneVerified;
    private boolean emailVerified;
    private String barcodeToken;
    private String barcodeQrUrl;
    private LoyaltyTierInfoResponse loyalty;
    private QuickActionCountersResponse quickActionCounters;
    private boolean hasMissingPointsNotice;
    private Instant createdAt;
}

