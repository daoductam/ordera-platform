package com.ordera.modules.catalog.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Danh mục nhãn đặc tính món ăn / thông tin ăn kiêng và sở thích.
 * Độc lập hoàn toàn với Spring Framework và JPA.
 */
@Getter
@RequiredArgsConstructor
public enum DietaryTag {

    GLUTEN_FREE("Gluten Free", "Không chứa gluten"),
    VEGAN("Vegan", "Món thuần chay"),
    HALAL("Halal", "Chứng nhận Halal"),
    NUT_FREE("Nut Free", "Không chứa hạt gây dị ứng"),
    POPULAR("Popular", "Món bán chạy được yêu thích"),
    SPICY("Spicy", "Vị cay nồng");

    private final String displayName;
    private final String description;

    public static DietaryTag fromString(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.trim().replace(" ", "_").toUpperCase();
        for (DietaryTag tag : values()) {
            if (tag.name().equalsIgnoreCase(normalized) || tag.displayName.equalsIgnoreCase(text.trim())) {
                return tag;
            }
        }
        return null;
    }
}
