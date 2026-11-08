package com.ordera.modules.auth.domain.model;

/**
 * Vai trò phân quyền trong hệ thống Ordera.
 * Độc lập hoàn toàn với Spring Security và JPA.
 */
public enum UserRole {
    CUSTOMER,
    STORE_STAFF,
    AGENCY_ADMIN,
    SYSTEM_SUPERADMIN
}

