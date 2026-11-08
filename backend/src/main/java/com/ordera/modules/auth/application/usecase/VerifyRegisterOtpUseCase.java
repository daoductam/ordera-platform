package com.ordera.modules.auth.application.usecase;

import com.ordera.modules.auth.presentation.dto.VerifyRegisterOtpRequest;
import com.ordera.modules.auth.presentation.dto.VerifyRegisterOtpResponse;

/**
 * Use Case xác thực mã OTP đăng ký và khởi tạo tài khoản Users + MemberProfile (New Member, 0 điểm).
 */
public interface VerifyRegisterOtpUseCase {

    VerifyRegisterOtpResponse execute(VerifyRegisterOtpRequest request);
}
