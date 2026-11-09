package com.ordera.modules.kiosk.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskResetSessionRequest {

    @NotNull(message = "storeId không được để trống")
    private UUID storeId;

    @NotBlank(message = "Lý do reset phiên không được để trống")
    private String reason;
}

