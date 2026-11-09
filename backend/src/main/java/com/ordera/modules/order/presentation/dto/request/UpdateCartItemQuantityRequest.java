package com.ordera.modules.order.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemQuantityRequest {

    @Min(value = 0, message = "Quantity must be at least 0")
    @Max(value = 99, message = "Quantity must not exceed 99")
    private int quantity;
}

