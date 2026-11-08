package com.ordera.modules.catalog.domain.constant;

import com.ordera.modules.catalog.domain.model.DietaryTag;

/**
 * Tập hợp các hằng số cấu hình nghiệp vụ chuẩn hóa cho Phân hệ Catalog & Thực đơn.
 * Đặt trong tầng Domain (domain/constant) theo chuẩn Clean Architecture tương tự AuthConstants và LoyaltyConstants,
 * giúp các UseCase, DTO Mapper và Controller tái sử dụng thống nhất, loại bỏ hoàn toàn magic numbers và magic strings.
 */
public final class CatalogConstants {

    private CatalogConstants() {
        // Ngăn chặn khởi tạo đối tượng tiện ích hằng số
    }

    // --- 1. Quy tắc tìm kiếm món ăn (Product Search Rules) ---
    /** Độ dài tối thiểu của từ khóa tìm kiếm món ăn (2 ký tự) */
    public static final int MIN_SEARCH_QUERY_LENGTH = 2;

    /** Thông điệp lỗi khi từ khóa tìm kiếm quá ngắn */
    public static final String ERR_SEARCH_QUERY_MIN_LENGTH = "Search query must contain at least 2 characters.";

    // --- 2. Nhãn món ăn & Phổ biến (Dietary & Popularity Tags) ---
    /** Tên nhãn món ăn phổ biến / bán chạy */
    public static final String TAG_POPULAR = DietaryTag.POPULAR.getDisplayName();

    /** Tên nhãn món ăn phổ biến dạng chữ hoa */
    public static final String TAG_POPULAR_UPPER = DietaryTag.POPULAR.name();

    /** Chuỗi JSON mảng rỗng mặc định */
    public static final String EMPTY_JSON_ARRAY = "[]";

    // --- 3. Thứ tự hiển thị mặc định (Display Ordering Defaults) ---
    /** Thứ tự hiển thị mặc định cho nhóm món ăn không có nhóm con */
    public static final int DEFAULT_DISPLAY_ORDER = 0;

    // --- 4. Thông điệp xác thực ID đầu vào (Validation Error Messages) ---
    public static final String ERR_STORE_ID_REQUIRED = "Store ID cannot be null.";
    public static final String ERR_PRODUCT_ID_REQUIRED = "Product ID cannot be null.";
    public static final String ERR_CATEGORY_ID_REQUIRED = "Category ID cannot be null.";
}

