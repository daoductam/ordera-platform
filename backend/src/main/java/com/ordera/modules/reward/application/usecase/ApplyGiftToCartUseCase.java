package com.ordera.modules.reward.application.usecase;

import com.ordera.modules.reward.presentation.dto.ApplyGiftRequest;
import com.ordera.modules.reward.presentation.dto.CartGiftCalculationResponse;

public interface ApplyGiftToCartUseCase {

    CartGiftCalculationResponse execute(ApplyGiftRequest request);
}
