package com.ordera.modules.catalog.presentation.dto;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record ModifierGroupResponse(
    UUID id,
    String name,
    int minSelection,
    int maxSelection,
    int displayOrder,
    List<ModifierResponse> modifiers
) {}
