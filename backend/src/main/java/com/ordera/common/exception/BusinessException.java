package com.ordera.common.exception;

import com.ordera.common.error.ErrorCode;
import lombok.Getter;

/**
 * Ngoại lệ nghiệp vụ gốc cho toàn hệ thống Ordera.
 * Mang theo ErrorCode chuẩn hóa và chi tiết lỗi bổ sung (nếu có).
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object details;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.details = null;
    }

    public BusinessException(ErrorCode errorCode, String customMessage, Object details) {
        super(customMessage);
        this.errorCode = errorCode;
        this.details = details;
    }

    public BusinessException(ErrorCode errorCode, Object details) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
}

