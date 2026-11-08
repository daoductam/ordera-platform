package com.ordera.modules.reward.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatePromoCodeRequest {

    @NotBlank
    @Size(min = 3, max = 32)
    private String code;

    @NotNull
    private UUID cartId;
}
