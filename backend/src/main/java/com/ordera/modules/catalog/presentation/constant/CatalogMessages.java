package com.ordera.modules.catalog.presentation.constant;

/**
 * Tập hợp các thông điệp phản hồi API chuẩn hóa cho tầng Presentation của Phân hệ Catalog.
 * Đặt trong tầng Presentation (presentation/constant) để tách biệt hoàn toàn với tầng Domain.
 */
public final class CatalogMessages {

    private CatalogMessages() {
        // Ngăn chặn khởi tạo đối tượng tiện ích thông điệp
    }

    public static final String MSG_STORE_MENU_RETRIEVED = "Store menu retrieved successfully";
    public static final String MSG_PRODUCT_DETAILS_RETRIEVED = "Product details retrieved successfully";
    public static final String MSG_PRODUCT_MODIFIERS_RETRIEVED = "Product modifiers retrieved successfully";
    public static final String MSG_SEARCH_COMPLETED = "Search completed";
    public static final String MSG_CATEGORIES_RETRIEVED = "Categories retrieved successfully";
    public static final String MSG_SUBCATEGORIES_RETRIEVED = "Subcategories retrieved successfully";
    public static final String MSG_PROMOTION_BANNERS_RETRIEVED = "Promotion banners retrieved successfully";
}

