package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
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

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final StoreRepository storeRepository;
    private final CustomerFavoriteStoreRepository customerFavoriteStoreRepository;
    private final TenantDtoMapper tenantDtoMapper;

    @Override
    public List<StoreSummaryResponse> execute(String keyword,
                                             FulfillmentType fulfillmentType,
                                             Double userLat,
                                             Double userLng,
                                             UUID currentUserId) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(TenantErrorCode.TENANT_HEADER_MISSING);
        }

        List<StoreJpaEntity> stores = storeRepository.searchStores(tenantId, keyword);

        // Lọc theo hình thức phục vụ (Fulfillment Type)
        if (fulfillmentType != null && fulfillmentType != FulfillmentType.ALL) {
            stores = stores.stream().filter(s -> switch (fulfillmentType) {
                case DINE_IN -> s.isAcceptsDinein();
                case TAKEAWAY -> s.isAcceptsPickup();
                case DELIVERY -> s.isAcceptsDelivery();
                case ALL -> true;
            }).toList();
        }

        // Lấy danh sách ID cửa hàng yêu thích nếu đã đăng nhập
        Set<UUID> favoriteStoreIds = Collections.emptySet();
        if (currentUserId != null) {
            favoriteStoreIds = customerFavoriteStoreRepository.findByTenantIdAndUserId(tenantId, currentUserId)
                    .stream()
                    .map(CustomerFavoriteStoreJpaEntity::getStoreId)
                    .collect(Collectors.toSet());
        }

        // Ánh xạ sang DTO kèm khoảng cách GPS Haversine
        final Set<UUID> finalFavIds = favoriteStoreIds;
        List<StoreSummaryResponse> result = new ArrayList<>();
        for (StoreJpaEntity store : stores) {
            Double distanceKm = null;
            if (userLat != null && userLng != null && store.getLatitude() != null && store.getLongitude() != null) {
                distanceKm = calculateHaversineDistance(
                        userLat,
                        userLng,
                        store.getLatitude().doubleValue(),
                        store.getLongitude().doubleValue()
                );
            }
            boolean isFav = finalFavIds.contains(store.getId());
            result.add(tenantDtoMapper.toStoreSummaryResponse(store, distanceKm, isFav));
        }

        // Sắp xếp theo khoảng cách tăng dần (gần nhất lên đầu) nếu có tọa độ GPS
        if (userLat != null && userLng != null) {
            result.sort(Comparator.comparing(
                    StoreSummaryResponse::getDistanceKm,
                    Comparator.nullsLast(Double::compareTo)
            ));
        }

        return result;
    }

    public static Double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        return Math.round(distance * 100.0) / 100.0;
    }
}
