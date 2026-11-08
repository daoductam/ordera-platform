package com.ordera.modules.catalog.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.catalog.application.usecase.GetProductDetailUseCase;
import com.ordera.modules.catalog.domain.constant.CatalogConstants;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.infrastructure.mapper.CatalogDtoMapper;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierGroupJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.entity.ProductModifierJpaEntity;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierGroupRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductModifierRepository;
import com.ordera.modules.catalog.infrastructure.persistence.repository.ProductRepository;
import com.ordera.modules.catalog.presentation.dto.ModifierGroupResponse;
import com.ordera.modules.catalog.presentation.dto.ModifierResponse;
import com.ordera.modules.catalog.presentation.dto.ProductDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProductDetailUseCaseImpl implements GetProductDetailUseCase {

    private final ProductRepository productRepository;
    private final ProductModifierGroupRepository modifierGroupRepository;
    private final ProductModifierRepository modifierRepository;
    private final CatalogDtoMapper catalogDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse execute(UUID productId) {
        UUID tenantId = resolveTenantId();
        ProductJpaEntity product = fetchProduct(productId, tenantId);

        List<ProductModifierGroupJpaEntity> groups = fetchModifierGroups(productId);
        List<ModifierGroupResponse> groupResponses = assembleModifierGroups(groups);

        return assembleProductDetail(product, groupResponses);
    }

    private UUID resolveTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(CatalogErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private ProductJpaEntity fetchProduct(UUID productId, UUID tenantId) {
        if (productId == null) {
            throw new BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND, CatalogConstants.ERR_PRODUCT_ID_REQUIRED);
        }
        return productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND,
                        "Product with ID '" + productId + "' does not exist."));
    }

    private List<ProductModifierGroupJpaEntity> fetchModifierGroups(UUID productId) {
        return modifierGroupRepository.findByProductIdOrderByDisplayOrderAsc(productId);
    }

    private List<ModifierGroupResponse> assembleModifierGroups(List<ProductModifierGroupJpaEntity> groups) {
        if (groups.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> groupIds = groups.stream().map(ProductModifierGroupJpaEntity::getId).toList();
        List<ProductModifierJpaEntity> modifiers = modifierRepository.findByGroupIdIn(groupIds);

        Map<UUID, List<ModifierResponse>> modifiersByGroup = modifiers.stream()
                .collect(Collectors.groupingBy(
                        ProductModifierJpaEntity::getGroupId,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                m -> ModifierResponse.builder()
                                        .id(m.getId())
                                        .name(m.getName())
                                        .extraPrice(m.getExtraPrice())
                                        .isDefault(m.isDefault())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        return groups.stream()
                .map(g -> ModifierGroupResponse.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .minSelection(g.getMinSelection())
                        .maxSelection(g.getMaxSelection())
                        .displayOrder(g.getDisplayOrder())
                        .modifiers(modifiersByGroup.getOrDefault(g.getId(), Collections.emptyList()))
                        .build())
                .toList();
    }

    private ProductDetailResponse assembleProductDetail(ProductJpaEntity product, List<ModifierGroupResponse> groupResponses) {
        List<String> tags = catalogDtoMapper.parseDietaryTags(product.getDietaryTags());

        return ProductDetailResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .originalPrice(product.getOriginalPrice())
                .piecesText(product.getPiecesText())
                .dietaryTags(tags)
                .imageUrl(product.getImageUrl())
                .vatRate(product.getVatRate())
                .isAvailable(product.isAvailable())
                .modifierGroups(groupResponses)
                .build();
    }
}
