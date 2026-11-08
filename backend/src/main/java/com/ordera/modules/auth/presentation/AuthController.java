package com.ordera.modules.auth.presentation;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.auth.application.usecase.*;
import com.ordera.modules.auth.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "03. Auth & Identity", description = "Xác thực không mật khẩu (OTP), Đăng ký hội viên & Quản lý phiên JWT")
public class AuthController {

    private final SendOtpUseCase sendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final SendRegisterOtpUseCase sendRegisterOtpUseCase;
    private final VerifyRegisterOtpUseCase verifyRegisterOtpUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @Operation(summary = "3.1 Gửi mã OTP đăng nhập", description = "Gửi mã xác thực 6 số qua SMS (Cooldown 120 giây)")
    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<SendOtpResponse>> sendLoginOtp(
            @Valid @RequestBody SendOtpRequest request) {
        SendOtpResponse response = sendOtpUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.ok("Mã xác thực OTP đã được gửi thành công qua tin nhắn SMS.", response));
    }

    @Operation(summary = "3.2 Xác thực mã OTP đăng nhập", description = "Kiểm tra mã OTP 6 số, cấp JWT và trả cờ isProfileComplete")
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyLoginOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse response = verifyOtpUseCase.execute(request);
        String message = response.isProfileComplete()
                ? "Xác thực mã OTP thành công. Đăng nhập hoàn tất."
                : "Xác thực thành công. Vui lòng hoàn tất thông tin cá nhân của bạn.";
        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    @Operation(summary = "3.3 Gửi mã OTP đăng ký mới", description = "Gửi mã xác thực đăng ký hội viên qua SMS hoặc Email")
    @PostMapping("/register/otp/send")
    public ResponseEntity<ApiResponse<SendRegisterOtpResponse>> sendRegisterOtp(
            @Valid @RequestBody SendRegisterOtpRequest request) {
        SendRegisterOtpResponse response = sendRegisterOtpUseCase.execute(request);
        String message = String.format("Mã xác thực đã được gửi thành công qua %s %s",
                response.getChannel(), response.getRecipientMasked());
        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    @Operation(summary = "3.4 Xác thực OTP đăng ký & Khởi tạo tài khoản", description = "Xác thực mã OTP đăng ký, kích hoạt tài khoản New Member (0 điểm) và cấp JWT")
    @PostMapping("/register/otp/verify")
    public ResponseEntity<ApiResponse<VerifyRegisterOtpResponse>> verifyRegisterOtp(
            @Valid @RequestBody VerifyRegisterOtpRequest request) {
        VerifyRegisterOtpResponse response = verifyRegisterOtpUseCase.execute(request);
        String message = String.format("Đăng ký thành viên thành công! Chào mừng %s.",
                response.getUser() != null ? response.getUser().getFullName() : "bạn");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(message, response));
    }

    @Operation(summary = "3.5 Cấp lại Access Token", description = "Sử dụng Refresh Token để cấp phát Access Token mới (Token Rotation)")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = refreshTokenUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.ok("Cấp mới Access Token thành công", response));
    }

    @Operation(summary = "3.6 Đăng xuất & Thu hồi phiên làm việc", description = "Đưa Token vào Redis Blacklist để hủy phiên làm việc ngay lập tức")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) LogoutRequest request) {
        logoutUseCase.execute(authHeader, request != null ? request : new LogoutRequest());
        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công. Phiên làm việc đã bị hủy.", null));
    }
}
