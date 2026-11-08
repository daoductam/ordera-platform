package com.ordera.modules.reward.application.usecase;

import com.ordera.modules.reward.presentation.dto.CartGiftCalculationResponse;
import com.ordera.modules.reward.presentation.dto.RemoveGiftRequest;

public interface RemoveGiftFromCartUseCase {

    CartGiftCalculationResponse execute(RemoveGiftRequest request);
}
