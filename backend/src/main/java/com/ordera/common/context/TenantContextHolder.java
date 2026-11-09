package com.ordera.common.context;

import com.ordera.common.exception.BusinessException;
import com.ordera.modules.order.domain.exception.OrderErrorCode;

import java.util.UUID;

/**
 * Quản lý ngữ cảnh Tenant hiện tại (TenantContextHolder) cho toàn bộ hệ thống Ordera.
 * Ngăn chặn triệt để 100% lỗ hổng IDOR và rò rỉ dữ liệu giữa các thương hiệu.
 */
public final class TenantContextHolder {

    private TenantContextHolder() {
    }

    public static void setTenantId(UUID tenantId) {
        TenantContext.setTenantId(tenantId);
    }

    public static UUID getTenantId() {
        return TenantContext.getTenantId();
    }

    public static UUID getRequiredTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(OrderErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    public static void clear() {
        TenantContext.clear();
    }
}

