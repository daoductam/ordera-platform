package com.ordera.modules.catalog.infrastructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordera.modules.catalog.domain.model.Category;
import com.ordera.modules.catalog.domain.model.Product;
import com.ordera.modules.catalog.domain.model.ProductModifier;
import com.ordera.modules.catalog.domain.model.ProductModifierGroup;
import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CatalogMapper {

    Category toDomain(CategoryJpaEntity entity);
    CategoryJpaEntity toEntity(Category domain);
    List<Category> toCategoryDomainList(List<CategoryJpaEntity> entities);

    @Mapping(target = "dietaryTags", expression = "java(parseDietaryTags(entity.getDietaryTags()))")
    Product toDomain(ProductJpaEntity entity);

    @Mapping(target = "dietaryTags", expression = "java(writeDietaryTags(domain.getDietaryTags()))")
    ProductJpaEntity toEntity(Product domain);

    List<Product> toProductDomainList(List<ProductJpaEntity> entities);

    ProductModifierGroup toDomain(ProductModifierGroupJpaEntity entity);
    ProductModifierGroupJpaEntity toEntity(ProductModifierGroup domain);
    List<ProductModifierGroup> toGroupDomainList(List<ProductModifierGroupJpaEntity> entities);

    ProductModifier toDomain(ProductModifierJpaEntity entity);
    ProductModifierJpaEntity toEntity(ProductModifier domain);
    List<ProductModifier> toModifierDomainList(List<ProductModifierJpaEntity> entities);

    default List<String> parseDietaryTags(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
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
            return "[]";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(tags);
        } catch (Exception e) {
            return "[]";
        }
    }
}
