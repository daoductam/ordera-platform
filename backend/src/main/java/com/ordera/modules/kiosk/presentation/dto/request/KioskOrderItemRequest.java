package com.ordera.modules.kiosk.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KioskOrderItemRequest {

    @NotNull(message = "productId không được để trống")
    private UUID productId;

    @Min(value = 1, message = "Số lượng món ăn tối thiểu là 1")
    private int quantity;

    private List<UUID> modifierIds;
}

