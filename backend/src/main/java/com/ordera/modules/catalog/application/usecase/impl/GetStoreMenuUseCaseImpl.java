package com.ordera.modules.catalog.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.GetStoreMenuUseCase;
import com.ordera.modules.catalog.domain.constant.CatalogConstants;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.mapper.CatalogDtoMapper;
import com.ordera.modules.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.SubcategoryJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.CategoryRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierGroupRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.SubcategoryRepository;
import com.ordera.modules.catalog.presentation.dto.MenuCategoryNode;
import com.ordera.modules.catalog.presentation.dto.MenuSubcategoryNode;
import com.ordera.modules.catalog.presentation.dto.ProductSummaryResponse;
import com.ordera.modules.catalog.presentation.dto.StoreMenuResponse;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetStoreMenuUseCaseImpl implements GetStoreMenuUseCase {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final ProductRepository productRepository;
    private final ProductModifierGroupRepository modifierGroupRepository;
    private final StoreRepository storeRepository;
    private final CatalogDtoMapper catalogDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public StoreMenuResponse execute(UUID storeId) {
        UUID tenantId = resolveTenantId();
        validateStore(storeId, tenantId);

        List<CategoryJpaEntity> categories = fetchCategories(tenantId);
        if (categories.isEmpty()) {
            return StoreMenuResponse.builder()
                    .storeId(storeId)
                    .categories(Collections.emptyList())
                    .build();
        }

        List<UUID> categoryIds = categories.stream().map(CategoryJpaEntity::getId).toList();
        List<SubcategoryJpaEntity> subcategories = fetchSubcategories(categoryIds);
        List<ProductJpaEntity> products = fetchProducts(tenantId);

        Set<UUID> productsWithModifiers = resolveProductsWithModifiers(products);
        Map<UUID, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(CategoryJpaEntity::getId, CategoryJpaEntity::getName));

        List<ProductSummaryResponse> productSummaries = convertToProductSummaries(products, categoryNameMap, productsWithModifiers);
        List<MenuCategoryNode> categoryNodes = buildMenuHierarchy(categories, subcategories, productSummaries);

        return StoreMenuResponse.builder()
                .storeId(storeId)
                .categories(categoryNodes)
                .build();
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(CatalogErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private void validateStore(UUID storeId, UUID tenantId) {
        if (storeId == null) {
            throw new BusinessException(CatalogErrorCode.STORE_NOT_FOUND, CatalogConstants.ERR_STORE_ID_REQUIRED);
        }
        storeRepository.findByIdAndTenantId(storeId, tenantId)
                .orElseThrow(() -> new BusinessException(CatalogErrorCode.STORE_NOT_FOUND,
                        "Store with ID '" + storeId + "' does not exist."));
    }

    private List<CategoryJpaEntity> fetchCategories(UUID tenantId) {
        return categoryRepository.findByTenantIdAndActiveOrderByDisplayOrderAsc(tenantId, true);
    }

    private List<SubcategoryJpaEntity> fetchSubcategories(List<UUID> categoryIds) {
        return subcategoryRepository.findByCategoryIdInAndActiveOrderByDisplayOrderAsc(categoryIds, true);
    }

    private List<ProductJpaEntity> fetchProducts(UUID tenantId) {
        return productRepository.findByTenantIdAndAvailableOrderByCreatedAtDesc(tenantId, true);
    }

    private Set<UUID> resolveProductsWithModifiers(List<ProductJpaEntity> products) {
        if (products.isEmpty()) {
            return Collections.emptySet();
        }
        List<UUID> productIds = products.stream().map(ProductJpaEntity::getId).toList();
        List<ProductModifierGroupJpaEntity> groups = modifierGroupRepository.findByProductIdInOrderByDisplayOrderAsc(productIds);
        return groups.stream().map(ProductModifierGroupJpaEntity::getProductId).collect(Collectors.toSet());
    }

    private List<ProductSummaryResponse> convertToProductSummaries(
            List<ProductJpaEntity> products,
            Map<UUID, String> categoryNameMap,
            Set<UUID> productsWithModifiers) {

        return products.stream().map(p -> {
            List<String> tags = catalogDtoMapper.parseDietaryTags(p.getDietaryTags());
            boolean isPopular = tags.stream().anyMatch(t ->
                    CatalogConstants.TAG_POPULAR.equalsIgnoreCase(t) || CatalogConstants.TAG_POPULAR_UPPER.equalsIgnoreCase(t));

            return ProductSummaryResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .basePrice(p.getBasePrice())
                    .originalPrice(p.getOriginalPrice())
                    .piecesText(p.getPiecesText())
                    .dietaryTags(tags)
                    .categoryName(categoryNameMap.getOrDefault(p.getCategoryId(), ""))
                    .imageUrl(p.getImageUrl())
                    .isPopular(isPopular)
                    .isAvailable(p.isAvailable())
                    .hasModifiers(productsWithModifiers.contains(p.getId()))
                    .build();
        }).toList();
    }

    private List<MenuCategoryNode> buildMenuHierarchy(
            List<CategoryJpaEntity> categories,
            List<SubcategoryJpaEntity> subcategories,
            List<ProductSummaryResponse> products) {

        Map<UUID, ProductJpaEntity> productEntityMap = productRepository.findByTenantId(TenantContext.getTenantId())
                .stream().collect(Collectors.toMap(ProductJpaEntity::getId, p -> p, (a, b) -> a));

        Map<UUID, List<SubcategoryJpaEntity>> subcategoriesByCategory = subcategories.stream()
                .collect(Collectors.groupingBy(SubcategoryJpaEntity::getCategoryId, LinkedHashMap::new, Collectors.toList()));

        Map<UUID, List<ProductSummaryResponse>> productsBySubcategory = new HashMap<>();
        Map<UUID, List<ProductSummaryResponse>> productsWithoutSubcategory = new HashMap<>();

        for (ProductSummaryResponse prod : products) {
            ProductJpaEntity entity = productEntityMap.get(prod.id());
            if (entity != null) {
                if (entity.getSubcategoryId() != null) {
                    productsBySubcategory.computeIfAbsent(entity.getSubcategoryId(), k -> new ArrayList<>()).add(prod);
                } else {
                    productsWithoutSubcategory.computeIfAbsent(entity.getCategoryId(), k -> new ArrayList<>()).add(prod);
                }
            }
        }

        List<MenuCategoryNode> result = new ArrayList<>();

        for (CategoryJpaEntity category : categories) {
            List<SubcategoryJpaEntity> subs = subcategoriesByCategory.getOrDefault(category.getId(), Collections.emptyList());
            List<MenuSubcategoryNode> subNodes = new ArrayList<>();

            for (SubcategoryJpaEntity sub : subs) {
                List<ProductSummaryResponse> subProducts = productsBySubcategory.getOrDefault(sub.getId(), Collections.emptyList());
                subNodes.add(MenuSubcategoryNode.builder()
                        .subcategoryId(sub.getId())
                        .subcategoryName(sub.getName())
                        .displayOrder(sub.getDisplayOrder())
                        .products(subProducts)
                        .build());
            }

            List<ProductSummaryResponse> directProducts = productsWithoutSubcategory.getOrDefault(category.getId(), Collections.emptyList());
            if (!directProducts.isEmpty()) {
                subNodes.add(MenuSubcategoryNode.builder()
                        .subcategoryId(null)
                        .subcategoryName(category.getName())
                        .displayOrder(CatalogConstants.DEFAULT_DISPLAY_ORDER)
                        .products(directProducts)
                        .build());
            }

            result.add(MenuCategoryNode.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getName())
                    .icon(category.getIcon())
                    .displayOrder(category.getDisplayOrder())
                    .subcategories(subNodes)
                    .build());
        }

        return result;
    }
}
