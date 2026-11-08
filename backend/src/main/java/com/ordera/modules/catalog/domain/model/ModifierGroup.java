package com.ordera.modules.catalog.domain.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Nhóm tùy chọn kèm món ăn (ví dụ: Size, Topping, Mức đường, Lượng đá).
 * Độc lập hoàn toàn với Spring Framework và JPA.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifierGroup {

    private UUID id;
    private UUID productId;
    private String name;
    @Builder.Default
    private int minSelection = 0;
    @Builder.Default
    private int maxSelection = 1;
    @Builder.Default
    private int displayOrder = 0;
    @Builder.Default
    private List<Modifier> modifiers = new ArrayList<>();
}
