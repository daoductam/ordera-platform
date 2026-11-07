package com.ordera.modules.tenant.domain.exception;

import java.util.UUID;

public class StoreNotFoundException extends RuntimeException {

    public StoreNotFoundException(UUID storeId) {
        super("Store with ID '" + storeId + "' does not exist in this tenant.");
    }

    public StoreNotFoundException(String message) {
        super(message);
    }
}

