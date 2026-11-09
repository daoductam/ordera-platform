package com.ordera.modules.kiosk.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskResetSessionResponse {

    private String kioskStatus;
    private int countdownDefaultSeconds;
}

