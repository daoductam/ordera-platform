package com.ordera.modules.tenant.application.usecase;

import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.BusinessException;
import com.ordera.modules.tenant.application.usecase.impl.GetFulfillmentOptionsUseCaseImpl;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.infrastructure.persistence.entity.StoreJpaEntity;
import com.ordera.modules.tenant.infrastructure.persistence.repository.StoreRepository;
import com.ordera.modules.tenant.presentation.dto.FulfillmentOptionsResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetFulfillmentOptionsUseCaseTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private GetFulfillmentOptionsUseCaseImpl useCase;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should throw TENANT_HEADER_MISSING when tenant is null")
    void should_throw_when_tenant_is_null() {
        TenantContext.clear();

        assertThatThrownBy(() -> useCase.execute(storeId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(TenantErrorCode.TENANT_HEADER_MISSING));
    }

    @Test
    @DisplayName("should return store specific options matching store settings")
    void should_return_store_specific_options() {
        StoreJpaEntity store = StoreJpaEntity.builder()
                .name("Linn Sushi Sentrum")
                .acceptsDinein(true)
                .acceptsPickup(true)
                .acceptsDelivery(false)
                .preparingTimeMinutes(25)
                .openingHours("11:00 - 13:00")
                .build();
        store.setId(storeId);

        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.of(store));

        FulfillmentOptionsResponse response = useCase.execute(storeId);

        assertThat(response).isNotNull();
        assertThat(response.getSupportedTypes()).hasSize(3);

        assertThat(response.getSupportedTypes())
                .filteredOn(t -> "DINE_IN".equals(t.getType()))
                .extracting("enabled").containsExactly(true);

        assertThat(response.getSupportedTypes())
                .filteredOn(t -> "TAKEAWAY".equals(t.getType()))
                .extracting("enabled").containsExactly(true);

        assertThat(response.getSupportedTypes())
                .filteredOn(t -> "DELIVERY".equals(t.getType()))
                .extracting("enabled").containsExactly(false);

        assertThat(response.getTimeOptions().getAsap().getEstimatedPreparingMinutes()).isEqualTo(25);
        assertThat(response.getTimeOptions().getSchedule().getAvailableSlots()).contains("11:00 - 11:30", "11:30 - 12:00", "12:00 - 12:30", "12:30 - 13:00");
    }

    @Test
    @DisplayName("should throw STORE_NOT_FOUND when store does not exist")
    void should_throw_when_store_not_found() {
        when(storeRepository.findByIdAndTenantId(storeId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(storeId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(TenantErrorCode.STORE_NOT_FOUND));
    }

    @Test
    @DisplayName("should return default tenant options when storeId is null")
    void should_return_default_options_when_storeId_null() {
        FulfillmentOptionsResponse response = useCase.execute(null);

        assertThat(response).isNotNull();
        assertThat(response.getSupportedTypes()).hasSize(3);
        assertThat(response.getSupportedTypes()).allMatch(FulfillmentOptionsResponse.SupportedTypeDto::isEnabled);
        assertThat(response.getTimeOptions().getAsap().getEstimatedPreparingMinutes()).isEqualTo(15);
    }
}

