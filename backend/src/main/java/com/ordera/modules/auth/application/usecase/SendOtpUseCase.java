package com.ordera.modules.auth.application.usecase;

import com.ordera.modules.auth.presentation.dto.SendOtpRequest;
import com.ordera.modules.auth.presentation.dto.SendOtpResponse;

/**
 * Use Case gửi mã xác thực OTP đăng nhập qua SMS (có kiểm soát cooldown 120s).
 */
public interface SendOtpUseCase {

    SendOtpResponse execute(SendOtpRequest request);
}

