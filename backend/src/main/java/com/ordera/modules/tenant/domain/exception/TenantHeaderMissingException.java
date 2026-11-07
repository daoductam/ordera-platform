package com.ordera.modules.tenant.domain.exception;

public class TenantHeaderMissingException extends RuntimeException {

    public TenantHeaderMissingException() {
        super("Required request header 'X-Tenant-ID' is missing.");
    }

    public TenantHeaderMissingException(String message) {
        super(message);
    }
}

