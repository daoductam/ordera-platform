package com.ordera.common.context;

import java.util.UUID;

/**
 * ThreadLocal context lưu trữ ID đại lý / thương hiệu hiện tại trong suốt chu kỳ Request.
 * Đảm bảo cách ly dữ liệu Multi-Tenant và chống lỗ hổng IDOR.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}

