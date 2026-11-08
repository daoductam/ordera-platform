package com.ordera.modules.catalog.domain.exception;

import com.ordera.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Danh mục mã lỗi nghiệp vụ riêng của Phân hệ Catalog & Thực đơn.
 * Tuân thủ RFC 7807 và chuẩn ErrorCode toàn hệ thống.
 * Độc lập hoàn toàn với Spring Framework và JPA.
 */
@Getter
@RequiredArgsConstructor
public enum CatalogErrorCode implements ErrorCode {

    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", 404, "Danh mục không tồn tại hoặc không thuộc thương hiệu hiện tại."),
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", 404, "Món ăn không tồn tại hoặc tạm ngưng phục vụ."),
    MODIFIER_GROUP_NOT_FOUND("MODIFIER_GROUP_NOT_FOUND", 404, "Nhóm tùy chọn món ăn không tồn tại."),
    INVALID_MODIFIER_SELECTION("INVALID_MODIFIER_SELECTION", 400, "Lựa chọn tùy chọn món không đáp ứng số lượng tối thiểu/tối đa."),
    STORE_NOT_FOUND("STORE_NOT_FOUND", 404, "Store does not exist in this tenant."),
    TENANT_HEADER_MISSING("TENANT_HEADER_MISSING", 400, "Header 'X-Tenant-ID' is required for multi-tenant data isolation."),
    INVALID_QUERY_PARAM("INVALID_QUERY_PARAM", 400, "Search query must contain at least 2 characters.");

    private final String code;
    private final int status;
    private final String defaultMessage;
}
