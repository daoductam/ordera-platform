package com.ordera.modules.auth.application.usecase;

import com.ordera.modules.auth.presentation.dto.SendRegisterOtpRequest;
import com.ordera.modules.auth.presentation.dto.SendRegisterOtpResponse;

/**
 * Use Case gửi mã xác thực OTP đăng ký tài khoản (hỗ trợ kênh SMS hoặc Email).
 */
public interface SendRegisterOtpUseCase {

    SendRegisterOtpResponse execute(SendRegisterOtpRequest request);
}
