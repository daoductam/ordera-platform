package com.ordera.common.error;

/**
 * Giao diện định nghĩa chuẩn mã lỗi cho toàn hệ thống.
 * Độc lập với Spring Framework để tầng Domain có thể sử dụng trực tiếp.
 */
public interface ErrorCode {

    /**
     * Mã định danh chuỗi của lỗi (ví dụ: TENANT_HEADER_MISSING, STORE_NOT_FOUND).
     */
    String getCode();

    /**
     * Mã trạng thái HTTP tương ứng (ví dụ: 400, 401, 403, 404, 409, 500).
     */
    int getStatus();

    /**
     * Thông điệp mặc định của lỗi.
     */
    String getDefaultMessage();
}

