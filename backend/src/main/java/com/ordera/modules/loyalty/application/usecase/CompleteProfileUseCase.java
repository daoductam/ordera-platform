package com.ordera.modules.loyalty.application.usecase;

import com.ordera.modules.loyalty.presentation.dto.CompleteProfileRequest;
import com.ordera.modules.loyalty.presentation.dto.CompleteProfileResponse;

public interface CompleteProfileUseCase {

    CompleteProfileResponse execute(CompleteProfileRequest request);
}

