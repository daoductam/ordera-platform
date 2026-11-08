package com.ordera.modules.reward.application.usecase;

import com.ordera.modules.reward.presentation.dto.ApplyVoucherRequest;
import com.ordera.modules.reward.presentation.dto.CartVoucherCalculationResponse;

public interface ApplyVoucherToCartUseCase {

    CartVoucherCalculationResponse execute(ApplyVoucherRequest request);
}
