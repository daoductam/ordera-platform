package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.GeoUtils;
import com.ordera.modules.tenant.application.usecase.GetFavoriteStoresUseCase;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFavoriteStoresUseCaseImpl implements GetFavoriteStoresUseCase {

    private final StoreRepository storeRepository;
    private final CustomerFavoriteStoreRepository customerFavoriteStoreRepository;
    private final TenantDtoMapper tenantDtoMapper;

    @Override
    public List<StoreSummaryResponse> execute(UUID userId, Double userLat, Double userLng) {
        UUID tenantId = validateAndGetTenantId();

        List<UUID> favoriteStoreIds = getFavoriteStoreIds(tenantId, userId);
        if (favoriteStoreIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<StoreJpaEntity> stores = findActiveStores(tenantId, favoriteStoreIds);
        List<StoreSummaryResponse> responses = mapToStoreResponses(stores, userLat, userLng);
        return sortByDistanceIfAvailable(responses, userLat, userLng);
    }

    private UUID validateAndGetTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(TenantErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private List<UUID> getFavoriteStoreIds(UUID tenantId, UUID userId) {
        return customerFavoriteStoreRepository.findByTenantIdAndUserId(tenantId, userId)
                .stream()
                .map(CustomerFavoriteStoreJpaEntity::getStoreId)
                .toList();
    }

    private List<StoreJpaEntity> findActiveStores(UUID tenantId, List<UUID> storeIds) {
        return storeRepository.findAllById(storeIds).stream()
                .filter(store -> tenantId.equals(store.getTenantId()) && store.isActive())
                .toList();
    }

    private List<StoreSummaryResponse> mapToStoreResponses(List<StoreJpaEntity> stores, Double userLat, Double userLng) {
        List<StoreSummaryResponse> responses = new ArrayList<>(stores.size());
        for (StoreJpaEntity store : stores) {
            Double distanceKm = calculateStoreDistance(store, userLat, userLng);
            responses.add(tenantDtoMapper.toStoreSummaryResponse(store, distanceKm, true));
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
