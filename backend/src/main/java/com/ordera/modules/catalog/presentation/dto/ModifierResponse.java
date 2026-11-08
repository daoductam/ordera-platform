package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ModifierResponse(
    UUID id,
    String name,
    BigDecimal extraPrice,
    boolean isDefault
) {}
