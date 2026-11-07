package com.ordera.modules.tenant.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private UUID id;
    private String code;
    private String name;
    private String slogan;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String status;
    private String currency;
    private BigDecimal defaultVatRate;
    private String supportPhone;
    private String supportEmail;
}

