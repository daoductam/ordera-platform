package com.ordera.modules.auth.application.usecase;

import com.ordera.modules.auth.presentation.dto.LogoutRequest;

/**
 * Use Case xử lý đăng xuất, thu hồi JWT vào Redis Blacklist.
 */
public interface LogoutUseCase {

    void execute(String authHeader, LogoutRequest request);
}
