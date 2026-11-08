package com.ordera.modules.auth.domain.model;

/**
 * Trạng thái tài khoản người dùng.
 * Độc lập với Spring Framework và JPA.
 */
public enum UserStatus {
    ACTIVE,
    LOCKED,
    DELETED
}

