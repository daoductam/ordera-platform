package com.ordera.modules.auth.application.usecase;

import com.ordera.modules.auth.presentation.dto.RefreshTokenRequest;
import com.ordera.modules.auth.presentation.dto.RefreshTokenResponse;

/**
 * Use Case cấp lại Access Token và xoay vòng Refresh Token.
 */
public interface RefreshTokenUseCase {

    RefreshTokenResponse execute(RefreshTokenRequest request);
}
