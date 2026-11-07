package com.ordera.modules.tenant.domain.exception;

import java.util.UUID;

public class TableNotFoundException extends RuntimeException {

    public TableNotFoundException(UUID tableId) {
        super("Table with ID '" + tableId + "' does not exist in this store.");
    }

    public TableNotFoundException(String message) {
        super(message);
    }
}

