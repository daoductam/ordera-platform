package com.ordera.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object lưu giữ cặp JWT Tokens cấp phát cho người dùng.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private long expiresIn;
}
