package com.ordera.modules.catalog.infrastructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordera.modules.catalog.domain.constant.CatalogConstants;
import com.ordera.modules.catalog.domain.model.*;
import com.ordera.modules.catalog.infrastructure.persistence.entity.*;
import com.ordera.modules.catalog.presentation.dto.*;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CatalogDtoMapper {

    // --- Category Mapping ---
    Category toDomain(CategoryJpaEntity entity);
    CategoryJpaEntity toEntity(Category domain);
    List<Category> toCategoryDomainList(List<CategoryJpaEntity> entities);

    CategoryResponse toCategoryResponse(Category domain);
    List<CategoryResponse> toCategoryResponseList(List<Category> domains);

    // --- Subcategory Mapping ---
    Subcategory toDomain(SubcategoryJpaEntity entity);
    SubcategoryJpaEntity toEntity(Subcategory domain);
    List<Subcategory> toSubcategoryDomainList(List<SubcategoryJpaEntity> entities);

    SubcategoryResponse toSubcategoryResponse(Subcategory domain);
    List<SubcategoryResponse> toSubcategoryResponseList(List<Subcategory> domains);

    // --- Product Mapping ---
    @Mapping(target = "dietaryTags", expression = "java(parseDietaryTags(entity.getDietaryTags()))")
    Product toDomain(ProductJpaEntity entity);

    @Mapping(target = "dietaryTags", expression = "java(writeDietaryTags(domain.getDietaryTags()))")
    ProductJpaEntity toEntity(Product domain);

    List<Product> toProductDomainList(List<ProductJpaEntity> entities);

    @Mapping(target = "isPopular", expression = "java(isProductPopular(domain))")
    @Mapping(target = "isAvailable", source = "available")
    @Mapping(target = "hasModifiers", expression = "java(domain.getModifierGroups() != null && !domain.getModifierGroups().isEmpty())")
    ProductSummaryResponse toProductSummaryResponse(Product domain);

    List<ProductSummaryResponse> toProductSummaryResponseList(List<Product> domains);

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "productName", source = "name")
    @Mapping(target = "isAvailable", source = "available")
    ProductDetailResponse toProductDetailResponse(Product domain);

    // --- ModifierGroup Mapping ---
    ModifierGroup toDomain(ProductModifierGroupJpaEntity entity);
    ProductModifierGroupJpaEntity toEntity(ModifierGroup domain);
    List<ModifierGroup> toGroupDomainList(List<ProductModifierGroupJpaEntity> entities);

    ModifierGroupResponse toModifierGroupResponse(ModifierGroup domain);
    List<ModifierGroupResponse> toModifierGroupResponseList(List<ModifierGroup> domains);

    // --- Modifier Mapping ---
    Modifier toDomain(ProductModifierJpaEntity entity);
    ProductModifierJpaEntity toEntity(Modifier domain);
    List<Modifier> toModifierDomainList(List<ProductModifierJpaEntity> entities);

    @Mapping(target = "isDefault", source = "default")
    ModifierResponse toModifierResponse(Modifier domain);
    List<ModifierResponse> toModifierResponseList(List<Modifier> domains);

    // --- PromotionBanner Mapping ---
    PromotionBanner toDomain(PromotionBannerJpaEntity entity);
    PromotionBannerJpaEntity toEntity(PromotionBanner domain);
    List<PromotionBanner> toBannerDomainList(List<PromotionBannerJpaEntity> entities);

    @Mapping(target = "ctaActionType", source = "actionType")
    @Mapping(target = "targetCategoryId", source = "targetId")
    PromotionBannerResponse toBannerResponse(PromotionBanner domain);
    List<PromotionBannerResponse> toBannerResponseList(List<PromotionBanner> domains);

    // --- Helper Methods ---
    default boolean isProductPopular(Product domain) {
        if (domain.getDietaryTags() == null) {
            return false;
        }
        return domain.getDietaryTags().stream()
                .anyMatch(tag -> CatalogConstants.TAG_POPULAR.equalsIgnoreCase(tag)
                        || CatalogConstants.TAG_POPULAR_UPPER.equalsIgnoreCase(tag));
    }

    default List<String> parseDietaryTags(String json) {
        if (json == null || json.isBlank() || json.equals(CatalogConstants.EMPTY_JSON_ARRAY)) {
            return Collections.emptyList();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    default String writeDietaryTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return CatalogConstants.EMPTY_JSON_ARRAY;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(tags);
        } catch (Exception e) {
            return CatalogConstants.EMPTY_JSON_ARRAY;
        }
    }
}
