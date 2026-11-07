package com.ordera.modules.tenant.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreSummaryResponse {

    private UUID id;
    private String name;
    private String address;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String coverImageUrl;
    private String logoUrl;
    private String phoneNumber;
    private String openingHoursText;
    private boolean isOpen;
    private Double distanceKm;
    private Integer preparingTimeMinutes;
    private List<String> supportedFulfillmentTypes;
    private boolean isFavorite;
}

