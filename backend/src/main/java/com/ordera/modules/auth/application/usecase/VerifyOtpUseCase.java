package com.ordera.modules.auth.application.usecase;

import com.ordera.modules.auth.presentation.dto.VerifyOtpRequest;
import com.ordera.modules.auth.presentation.dto.VerifyOtpResponse;

/**
 * Use Case xác thực mã OTP đăng nhập, kiểm tra số lần thử và cấp phát JWT Token.
 */
public interface VerifyOtpUseCase {

    VerifyOtpResponse execute(VerifyOtpRequest request);
}

