package com.ordera.modules.tenant.domain.exception;

public class InvalidQrTokenException extends RuntimeException {

    public InvalidQrTokenException(String qrToken) {
        super("Mã QR bàn ăn '" + qrToken + "' không hợp lệ hoặc không thuộc chi nhánh này.");
    }
}

