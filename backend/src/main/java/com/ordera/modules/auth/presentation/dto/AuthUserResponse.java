package com.ordera.modules.auth.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthUserResponse {

    private UUID id;
    private String phoneNumber;
    private String email;
    private String fullName;
    private String role;
    private boolean phoneVerified;
    private boolean emailVerified;
    private LocalDate dateOfBirth;
    private String tierName;
    private Integer currentPoints;
    private Integer lifetimePoints;
}

