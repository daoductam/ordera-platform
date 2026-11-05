package com.ordera.modules.catalog.infrastructure.mapper;

import com.ordera.modules.catalog.domain.model.Category;
import com.ordera.modules.catalog.domain.model.Product;
import com.ordera.modules.catalog.domain.model.ProductModifier;
import com.ordera.modules.catalog.domain.model.ProductModifierGroup;
import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CatalogMapper {

    Category toDomain(CategoryJpaEntity entity);
    CategoryJpaEntity toEntity(Category domain);
    List<Category> toCategoryDomainList(List<CategoryJpaEntity> entities);

    Product toDomain(ProductJpaEntity entity);
    ProductJpaEntity toEntity(Product domain);
    List<Product> toProductDomainList(List<ProductJpaEntity> entities);

    ProductModifierGroup toDomain(ProductModifierGroupJpaEntity entity);
    ProductModifierGroupJpaEntity toEntity(ProductModifierGroup domain);
    List<ProductModifierGroup> toGroupDomainList(List<ProductModifierGroupJpaEntity> entities);

    ProductModifier toDomain(ProductModifierJpaEntity entity);
    ProductModifierJpaEntity toEntity(ProductModifier domain);
    List<ProductModifier> toModifierDomainList(List<ProductModifierJpaEntity> entities);
}

