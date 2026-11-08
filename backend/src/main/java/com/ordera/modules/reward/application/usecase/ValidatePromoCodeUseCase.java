package com.ordera.modules.reward.application.usecase;

import com.ordera.modules.reward.presentation.dto.ValidatePromoCodeRequest;
import com.ordera.modules.reward.presentation.dto.ValidatePromoCodeResponse;

public interface ValidatePromoCodeUseCase {

    ValidatePromoCodeResponse execute(ValidatePromoCodeRequest request);
}

