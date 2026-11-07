package com.ordera.modules.tenant.domain.exception;

import com.ordera.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Danh mục mã lỗi nghiệp vụ riêng của Phân hệ Tenant & Store.
 */
@Getter
@RequiredArgsConstructor
public enum TenantErrorCode implements ErrorCode {

    TENANT_HEADER_MISSING("TENANT_HEADER_MISSING", 400, "Required request header 'X-Tenant-ID' is missing."),
    INVALID_QUERY_PARAM("INVALID_QUERY_PARAM", 400, "Invalid query parameter."),
    UNAUTHORIZED("UNAUTHORIZED", 401, "Vui lòng đăng nhập để thực hiện thao tác này."),
    TENANT_NOT_FOUND("TENANT_NOT_FOUND", 404, "No active tenant found with the specified ID."),
    STORE_NOT_FOUND("STORE_NOT_FOUND", 404, "Store does not exist in this tenant."),
    TABLE_NOT_FOUND("TABLE_NOT_FOUND", 404, "Table does not exist in this store."),
    INVALID_QR_TOKEN("INVALID_QR_TOKEN", 404, "Mã QR bàn ăn không hợp lệ hoặc không thuộc chi nhánh này.");

    private final String code;
    private final int status;
    private final String defaultMessage;
}

