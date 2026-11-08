package com.ordera.modules.loyalty.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileResponse {

    private UUID memberId;
    private String fullName;
    private String email;

    @JsonProperty("isProfileComplete")
    private boolean isProfileComplete;

    private String currentTier;
    private int currentPoints;
    private Instant updatedAt;
}
