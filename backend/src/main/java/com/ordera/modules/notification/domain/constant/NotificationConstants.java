package com.ordera.modules.notification.domain.constant;

import lombok.experimental.UtilityClass;

import java.util.Set;

/**
 * Hằng số nghiệp vụ cho Phân hệ Thông Báo & Hỗ Trợ Khách Hàng.
 * Cấm tuyệt đối magic numbers/strings trong mã nguồn nghiệp vụ.
 */
@UtilityClass
public class NotificationConstants {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;

    public static final String TICKET_CODE_PREFIX = "INQ-";
    public static final String TICKET_DATE_FORMAT = "yyyyMMdd";
    public static final String TICKET_CODE_FORMAT = "%s%s-%03d";

    public static final int MIN_INQUIRY_NOTE_LENGTH = 5;
    public static final int MAX_INQUIRY_NOTE_LENGTH = 500;

    public static final long MAX_RECEIPT_IMAGE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB

    public static final Set<String> ALLOWED_ATTACHMENT_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    public static final String DEFAULT_CATEGORY = "Messages";
    public static final String CATEGORY_NEWS = "News";
    public static final String CATEGORY_MESSAGES = "Messages";

    public static final String PHONE_REGEX = "^(\\+?[0-9]{8,15})$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
}

