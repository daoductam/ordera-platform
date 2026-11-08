package com.ordera.modules.catalog.presentation;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.catalog.application.usecase.GetPromotionBannersUseCase;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.presentation.dto.PromotionBannerResponse;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromotionController WebMvc Tests")
class PromotionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetPromotionBannersUseCase getPromotionBannersUseCase;

    @InjectMocks
    private PromotionController promotionController;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(promotionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId}/promotions/banners - should return 200 with banners")
    void should_return_200_when_getting_banners() throws Exception {
        PromotionBannerResponse banner = PromotionBannerResponse.builder()
                .id(UUID.randomUUID())
                .title("Summer Sales: 20% Off")
                .subtitle("Áp dụng cho Sushi")
                .imageUrl("https://storage.ordera.no/summer.jpg")
                .ctaLabel("Learn more")
                .ctaActionType("NAVIGATE_CATEGORY")
                .targetCategoryId(UUID.randomUUID())
                .active(true)
                .displayOrder(0)
                .build();

        when(getPromotionBannersUseCase.execute(storeId)).thenReturn(List.of(banner));

        mockMvc.perform(get("/api/v1/stores/{storeId}/promotions/banners", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Summer Sales: 20% Off"))
                .andExpect(jsonPath("$.data[0].ctaActionType").value("NAVIGATE_CATEGORY"));
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId}/promotions/banners - should return 404 when store not found")
    void should_return_404_when_store_not_found() throws Exception {
        when(getPromotionBannersUseCase.execute(storeId))
                .thenThrow(new BusinessException(CatalogErrorCode.STORE_NOT_FOUND, "Store does not exist."));

        mockMvc.perform(get("/api/v1/stores/{storeId}/promotions/banners", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"));
    }
}
