package com.ordera.modules.tenant.presentation;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.tenant.application.usecase.*;
import com.ordera.modules.tenant.domain.exception.TenantErrorCode;
import com.ordera.modules.tenant.domain.model.FulfillmentType;
import com.ordera.modules.tenant.presentation.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreController WebMvc Tests")
class StoreControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetStoresListUseCase getStoresListUseCase;

    @Mock
    private GetStoreDetailUseCase getStoreDetailUseCase;

    @Mock
    private GetStoreTablesUseCase getStoreTablesUseCase;

    @Mock
    private ResolveTableQrUseCase resolveTableQrUseCase;

    @Mock
    private ToggleFavoriteStoreUseCase toggleFavoriteStoreUseCase;

    @Mock
    private GetFavoriteStoresUseCase getFavoriteStoresUseCase;

    @Mock
    private GetFulfillmentOptionsUseCase getFulfillmentOptionsUseCase;

    @InjectMocks
    private StoreController storeController;

    private final UUID tenantId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    private final UUID storeId = UUID.fromString("b1eebc99-9c0b-4ef8-bb6d-6bb9bd380b01");
    private final UUID userId = UUID.fromString("c1eebc99-9c0b-4ef8-bb6d-6bb9bd380c01");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(storeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/stores - should return 200 with list of stores")
    void should_return_200_when_listing_stores() throws Exception {
        StoreSummaryResponse summary = StoreSummaryResponse.builder()
                .id(storeId)
                .name("Linn Sushi Sentrum")
                .address("Prinsens gate 10C, 0153 Oslo")
                .city("Oslo")
                .distanceKm(0.35)
                .isOpen(true)
                .supportedFulfillmentTypes(List.of("TAKEAWAY", "DINE_IN", "DELIVERY"))
                .isFavorite(false)
                .build();

        when(getStoresListUseCase.execute(isNull(), eq(FulfillmentType.ALL), isNull(), isNull(), isNull()))
                .thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/stores")
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(storeId.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Linn Sushi Sentrum"))
                .andExpect(jsonPath("$.data[0].distanceKm").value(0.35));
    }

    @Test
    @DisplayName("GET /api/v1/stores - should return 400 when invalid fulfillmentType is provided")
    void should_return_400_when_invalid_fulfillment_type() throws Exception {
        mockMvc.perform(get("/api/v1/stores")
                        .header("X-Tenant-ID", tenantId.toString())
                        .param("fulfillmentType", "EXPRESS_PICKUP")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_QUERY_PARAM"));
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId} - should return 200 with store details")
    void should_return_200_when_store_found() throws Exception {
        StoreDetailResponse detail = StoreDetailResponse.builder()
                .id(storeId)
                .name("Linn Sushi Sentrum")
                .address("Prinsens gate 10C, 0153 Oslo")
                .deliveryFee(new BigDecimal("39.00"))
                .deliveryRadiusKm(new BigDecimal("5.00"))
                .isOpen(true)
                .build();

        when(getStoreDetailUseCase.execute(storeId)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/stores/{storeId}", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(storeId.toString()))
                .andExpect(jsonPath("$.data.deliveryFee").value(39.00));
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId} - should return 404 when store not found")
    void should_return_404_when_store_not_found() throws Exception {
        when(getStoreDetailUseCase.execute(storeId)).thenThrow(new BusinessException(TenantErrorCode.STORE_NOT_FOUND));

        mockMvc.perform(get("/api/v1/stores/{storeId}", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId}/tables - should return 200 with tables")
    void should_return_200_with_tables() throws Exception {
        StoreTableResponse table = StoreTableResponse.builder()
                .id(UUID.randomUUID())
                .storeId(storeId)
                .tableNumber("Table 05")
                .zone("Tầng 1")
                .seatingCapacity(4)
                .isOccupied(false)
                .build();

        when(getStoreTablesUseCase.execute(storeId)).thenReturn(List.of(table));

        mockMvc.perform(get("/api/v1/stores/{storeId}/tables", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].tableNumber").value("Table 05"));
    }

    @Test
    @DisplayName("GET /api/v1/stores/tables/qr/{qrToken} - should return 200 with resolved table")
    void should_return_200_when_qr_token_resolved() throws Exception {
        ResolvedTableQrResponse resolved = ResolvedTableQrResponse.builder()
                .storeId(storeId)
                .storeName("Linn Sushi Sentrum")
                .tableId(UUID.randomUUID())
                .tableNumber("Table 05")
                .zone("Tầng 1")
                .capacity(4)
                .isOccupied(false)
                .build();

        when(resolveTableQrUseCase.execute("qr_linn_sentrum_table_05")).thenReturn(resolved);

        mockMvc.perform(get("/api/v1/stores/tables/qr/{qrToken}", "qr_linn_sentrum_table_05")
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tableNumber").value("Table 05"))
                .andExpect(jsonPath("$.data.storeName").value("Linn Sushi Sentrum"));
    }

    @Test
    @DisplayName("GET /api/v1/stores/tables/qr/{qrToken} - should return 404 when QR token is invalid")
    void should_return_404_when_qr_token_invalid() throws Exception {
        when(resolveTableQrUseCase.execute("invalid_token")).thenThrow(new BusinessException(TenantErrorCode.INVALID_QR_TOKEN));

        mockMvc.perform(get("/api/v1/stores/tables/qr/{qrToken}", "invalid_token")
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_QR_TOKEN"));
    }

    @Test
    @DisplayName("GET /api/v1/stores/fulfillment-options - should return 200 with options")
    void should_return_200_fulfillment_options() throws Exception {
        when(getFulfillmentOptionsUseCase.execute(isNull())).thenReturn(
                FulfillmentOptionsResponse.builder()
                        .supportedTypes(List.of(
                                FulfillmentOptionsResponse.SupportedTypeDto.builder()
                                        .type("DINE_IN").label("Dine In").enabled(true).build()
                        ))
                        .timeOptions(FulfillmentOptionsResponse.TimeOptionsDto.builder()
                                .asap(FulfillmentOptionsResponse.AsapOptionDto.builder()
                                        .label("As soon as possible").build())
                                .build())
                        .build()
        );

        mockMvc.perform(get("/api/v1/stores/fulfillment-options")
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.supportedTypes").isArray())
                .andExpect(jsonPath("$.data.timeOptions.asap.label").value("As soon as possible"));
    }

    @Test
    @DisplayName("POST /api/v1/stores/{storeId}/favorite - should return 401 when user is not logged in")
    void should_return_401_when_adding_favorite_without_login() throws Exception {
        mockMvc.perform(post("/api/v1/stores/{storeId}/favorite", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("POST /api/v1/stores/{storeId}/favorite - should return 200 when favorited successfully")
    void should_return_200_when_favorited_successfully() throws Exception {
        ToggleFavoriteResponse response = ToggleFavoriteResponse.builder()
                .storeId(storeId)
                .isFavorite(true)
                .build();

        when(toggleFavoriteStoreUseCase.execute(storeId, userId, true)).thenReturn(response);

        mockMvc.perform(post("/api/v1/stores/{storeId}/favorite", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.storeId").value(storeId.toString()))
                .andExpect(jsonPath("$.data.favorite").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/stores/{storeId}/favorite - should return 200 when unfavorited successfully")
    void should_return_200_when_unfavorited_successfully() throws Exception {
        ToggleFavoriteResponse response = ToggleFavoriteResponse.builder()
                .storeId(storeId)
                .isFavorite(false)
                .build();

        when(toggleFavoriteStoreUseCase.execute(storeId, userId, false)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/stores/{storeId}/favorite", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.favorite").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/stores/favorites - should return 200 with favorite stores")
    void should_return_200_when_getting_favorites() throws Exception {
        StoreSummaryResponse summary = StoreSummaryResponse.builder()
                .id(storeId)
                .name("Linn Sushi Sentrum")
                .isFavorite(true)
                .build();

        when(getFavoriteStoresUseCase.execute(userId, null, null)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/v1/stores/favorites")
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(storeId.toString()))
                .andExpect(jsonPath("$.data[0].favorite").value(true));
    }
}
