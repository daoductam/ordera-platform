package com.ordera.modules.catalog.application.usecase;

import com.ordera.modules.catalog.presentation.dto.ModifierGroupResponse;

import java.util.List;
import java.util.UUID;

/**
 * Lấy danh sách nhóm modifiers và options chi tiết của một món ăn.
 */
public interface GetProductModifiersUseCase {

    List<ModifierGroupResponse> execute(UUID productId);
}
