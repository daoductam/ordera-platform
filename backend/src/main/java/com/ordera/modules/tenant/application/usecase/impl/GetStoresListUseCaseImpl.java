package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.GeoUtils;
import com.ordera.modules.tenant.application.usecase.GetStoresListUseCase;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.domain.model.FulfillmentType;
import com.ordera.modules.tenant.infrastructure.mapper.TenantDtoMapper;
import com.ordera.modules.tenant.infrastructure.persistence.entity.CustomerFavoriteStoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.CustomerFavoriteStoreRepository;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.presentation.dto.StoreSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStoresListUseCaseImpl implements GetStoresListUseCase {

    private final StoreRepository storeRepository;
    private final CustomerFavoriteStoreRepository customerFavoriteStoreRepository;
    private final TenantDtoMapper tenantDtoMapper;

    @Override
    public List<StoreSummaryResponse> execute(String keyword,
                                             FulfillmentType fulfillmentType,
                                             Double userLat,
                                             Double userLng,
                                             UUID currentUserId) {
        UUID tenantId = validateAndGetTenantId();

        List<StoreJpaEntity> stores = searchStores(tenantId, keyword);
        List<StoreJpaEntity> filteredStores = filterByFulfillment(stores, fulfillmentType);
        Set<UUID> favoriteStoreIds = resolveFavoriteStoreIds(tenantId, currentUserId);

        List<StoreSummaryResponse> responses = mapToStoreResponses(filteredStores, favoriteStoreIds, userLat, userLng);
        return sortByDistanceIfAvailable(responses, userLat, userLng);
    }

    private UUID validateAndGetTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(TenantErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private List<StoreJpaEntity> searchStores(UUID tenantId, String keyword) {
        String trimmedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return storeRepository.searchStores(tenantId, trimmedKeyword);
    }

    private List<StoreJpaEntity> filterByFulfillment(List<StoreJpaEntity> stores, FulfillmentType fulfillmentType) {
        if (fulfillmentType == null || fulfillmentType == FulfillmentType.ALL) {
            return stores;
        }
        return stores.stream().filter(store -> switch (fulfillmentType) {
            case DINE_IN -> store.isAcceptsDinein();
            case TAKEAWAY -> store.isAcceptsPickup();
            case DELIVERY -> store.isAcceptsDelivery();
            case ALL -> true;
        }).toList();
    }

    private Set<UUID> resolveFavoriteStoreIds(UUID tenantId, UUID currentUserId) {
        if (currentUserId == null) {
            return Collections.emptySet();
        }
        return customerFavoriteStoreRepository.findByTenantIdAndUserId(tenantId, currentUserId)
                .stream()
                .map(CustomerFavoriteStoreJpaEntity::getStoreId)
                .collect(Collectors.toSet());
    }

    private List<StoreSummaryResponse> mapToStoreResponses(List<StoreJpaEntity> stores,
                                                           Set<UUID> favoriteStoreIds,
                                                           Double userLat,
                                                           Double userLng) {
        List<StoreSummaryResponse> responses = new ArrayList<>(stores.size());
        for (StoreJpaEntity store : stores) {
            Double distanceKm = calculateStoreDistance(store, userLat, userLng);
            boolean isFavorite = favoriteStoreIds.contains(store.getId());
            responses.add(tenantDtoMapper.toStoreSummaryResponse(store, distanceKm, isFavorite));
        }
        return responses;
    }

    private Double calculateStoreDistance(StoreJpaEntity store, Double userLat, Double userLng) {
        if (userLat != null && userLng != null && store.getLatitude() != null && store.getLongitude() != null) {
            return GeoUtils.calculateDistanceKm(
                    userLat,
                    userLng,
                    store.getLatitude().doubleValue(),
                    store.getLongitude().doubleValue()
            );
        }
        return null;
    }

    private List<StoreSummaryResponse> sortByDistanceIfAvailable(List<StoreSummaryResponse> responses,
                                                                 Double userLat,
                                                                 Double userLng) {
        if (userLat != null && userLng != null) {
            responses.sort(Comparator.comparing(
                    StoreSummaryResponse::getDistanceKm,
                    Comparator.nullsLast(Double::compareTo)
            ));
        }
        return responses;
    }
}
