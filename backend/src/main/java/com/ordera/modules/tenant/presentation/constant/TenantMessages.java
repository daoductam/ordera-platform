package com.ordera.modules.tenant.presentation.constant;

/**
 * Presentation layer messages for Tenant & Store endpoints.
 * User-facing response envelope messages and API error descriptions.
 */
public final class TenantMessages {

    private TenantMessages() {
        // Prevent instantiation of constant utility class
    }

    public static final String MSG_STORES_RETRIEVED = "Stores retrieved successfully";
    public static final String MSG_STORE_DETAIL_RETRIEVED = "Store details retrieved successfully";
    public static final String MSG_TABLES_RETRIEVED = "Tables retrieved successfully";
    public static final String MSG_TABLE_RESOLVED = "Table resolved successfully";
    public static final String MSG_FULFILLMENT_OPTIONS_RETRIEVED = "Fulfillment options retrieved successfully";
    public static final String MSG_STORE_FAVORITE_ADDED = "Store added to favorites successfully";
    public static final String MSG_STORE_FAVORITE_REMOVED = "Store removed from favorites successfully";
    public static final String MSG_FAVORITE_STORES_RETRIEVED = "Favorite stores retrieved successfully";
    public static final String MSG_TENANT_DETAILS_RETRIEVED = "Tenant details retrieved successfully";

    public static final String ERR_INVALID_FULFILLMENT_TYPE = "Giá trị 'fulfillmentType' không hợp lệ. Chỉ chấp nhận ALL, DINE_IN, TAKEAWAY, DELIVERY.";
    public static final String ERR_UNAUTHORIZED_FAVORITE_ADD = "Vui lòng đăng nhập để lưu chi nhánh vào danh sách yêu thích.";
    public static final String ERR_UNAUTHORIZED_FAVORITE_REMOVE = "Vui lòng đăng nhập để gỡ chi nhánh khỏi danh sách yêu thích.";
    public static final String ERR_UNAUTHORIZED_FAVORITE_VIEW = "Vui lòng đăng nhập để xem danh sách yêu thích.";
}

