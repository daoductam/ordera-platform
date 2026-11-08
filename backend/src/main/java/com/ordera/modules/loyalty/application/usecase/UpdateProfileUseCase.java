package com.ordera.modules.loyalty.application.usecase;

import com.ordera.modules.loyalty.presentation.dto.UpdateProfileRequest;
import com.ordera.modules.loyalty.presentation.dto.UpdateProfileResponse;

public interface UpdateProfileUseCase {

    UpdateProfileResponse execute(UpdateProfileRequest request);
}

