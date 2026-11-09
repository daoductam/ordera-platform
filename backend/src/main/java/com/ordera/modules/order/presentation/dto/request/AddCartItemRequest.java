package com.ordera.modules.order.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCartItemRequest {

    private UUID storeId;

    @NotNull(message = "productId must not be null")
    private UUID productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 99, message = "Quantity must not exceed 99")
    @Builder.Default
    private int quantity = 1;

    @Builder.Default
    private List<UUID> selectedModifierIds = new ArrayList<>();

    @Size(max = 255, message = "Special instructions must not exceed 255 characters")
    private String specialInstructions;
}
