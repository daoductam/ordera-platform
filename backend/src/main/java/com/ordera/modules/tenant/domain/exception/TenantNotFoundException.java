package com.ordera.modules.tenant.domain.exception;

import java.util.UUID;

public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(UUID tenantId) {
        super("No active tenant found with ID '" + tenantId + "'.");
    }

    public TenantNotFoundException(String message) {
        super(message);
    }
}

