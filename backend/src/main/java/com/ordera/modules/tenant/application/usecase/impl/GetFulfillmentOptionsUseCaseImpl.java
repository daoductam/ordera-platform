package com.ordera.modules.tenant.application.usecase.impl;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.DateTimeUtils;
import com.ordera.modules.tenant.application.usecase.GetFulfillmentOptionsUseCase;
import com.ordera.modules.tenant.domain.constant.TenantConstants;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.presentation.dto.FulfillmentOptionsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFulfillmentOptionsUseCaseImpl implements GetFulfillmentOptionsUseCase {

    private final StoreRepository storeRepository;

    @Override
    public FulfillmentOptionsResponse execute(UUID storeId) {
        UUID tenantId = validateAndGetTenantId();

        if (storeId != null) {
            StoreJpaEntity store = findStoreOrThrow(storeId, tenantId);
            return buildStoreSpecificOptions(store);
        }

        return buildDefaultTenantOptions();
    }

    private UUID validateAndGetTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(TenantErrorCode.TENANT_HEADER_MISSING);
        }
        return tenantId;
    }

    private StoreJpaEntity findStoreOrThrow(UUID storeId, UUID tenantId) {
        return storeRepository.findByIdAndTenantId(storeId, tenantId)
                .orElseThrow(() -> new BusinessException(
                        TenantErrorCode.STORE_NOT_FOUND,
                        "Store with ID '" + storeId + "' does not exist in this tenant."
                ));
    }

    private FulfillmentOptionsResponse buildStoreSpecificOptions(StoreJpaEntity store) {
        boolean dineInEnabled = store.isAcceptsDinein();
        boolean takeawayEnabled = store.isAcceptsPickup();
        boolean deliveryEnabled = store.isAcceptsDelivery();
        int prepMinutes = store.getPreparingTimeMinutes() != null ? store.getPreparingTimeMinutes() : TenantConstants.DEFAULT_PREPARING_TIME_MINUTES;
        List<String> availableSlots = DateTimeUtils.generateTimeSlots(store.getOpeningHours());

        return buildResponse(dineInEnabled, takeawayEnabled, deliveryEnabled, prepMinutes, availableSlots);
    }

    private FulfillmentOptionsResponse buildDefaultTenantOptions() {
        return buildResponse(true, true, true, TenantConstants.DEFAULT_PREPARING_TIME_MINUTES, DateTimeUtils.DEFAULT_SLOTS);
    }

    private FulfillmentOptionsResponse buildResponse(boolean dineIn,
                                                     boolean takeaway,
                                                     boolean delivery,
                                                     int prepMinutes,
                                                     List<String> slots) {
        return FulfillmentOptionsResponse.builder()
                .supportedTypes(List.of(
                        FulfillmentOptionsResponse.SupportedTypeDto.builder()
                                .type("DINE_IN").label("Dine In").icon("utensils").enabled(dineIn).build(),
                        FulfillmentOptionsResponse.SupportedTypeDto.builder()
                                .type("TAKEAWAY").label("Takeaway").icon("shopping-bag").enabled(takeaway).build(),
                        FulfillmentOptionsResponse.SupportedTypeDto.builder()
                                .type("DELIVERY").label("Delivery").icon("bike").enabled(delivery).build()
                ))
                .timeOptions(FulfillmentOptionsResponse.TimeOptionsDto.builder()
                        .asap(FulfillmentOptionsResponse.AsapOptionDto.builder()
                                .label("As soon as possible").estimatedPreparingMinutes(prepMinutes).build())
                        .schedule(FulfillmentOptionsResponse.ScheduleOptionDto.builder()
                                .label("Schedule")
                                .sublabel("Select a time")
                                .allowSameDayOnly(true)
                                .availableSlots(slots)
                                .build())
                        .build())
                .build();
    }
}

