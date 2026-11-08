package com.ordera.modules.catalog.presentation;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.catalog.application.usecase.GetProductDetailUseCase;
import com.ordera.modules.catalog.application.usecase.GetProductModifiersUseCase;
import com.ordera.modules.catalog.application.usecase.GetStoreMenuUseCase;
import com.ordera.modules.catalog.application.usecase.SearchProductsUseCase;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.presentation.dto.ModifierGroupResponse;
import com.ordera.modules.catalog.presentation.dto.ModifierResponse;
import com.ordera.modules.catalog.presentation.dto.ProductDetailResponse;
import com.ordera.modules.catalog.presentation.dto.StoreMenuResponse;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController WebMvc Tests")
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetStoreMenuUseCase getStoreMenuUseCase;

    @Mock
    private GetProductDetailUseCase getProductDetailUseCase;

    @Mock
    private GetProductModifiersUseCase getProductModifiersUseCase;

    @Mock
    private SearchProductsUseCase searchProductsUseCase;

    @InjectMocks
    private ProductController productController;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId}/menu - should return 200 with menu tree")
    void should_return_200_when_getting_menu() throws Exception {
        StoreMenuResponse response = StoreMenuResponse.builder()
                .storeId(storeId)
                .categories(Collections.emptyList())
                .build();

        when(getStoreMenuUseCase.execute(storeId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/stores/{storeId}/menu", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.storeId").value(storeId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - should return 200 with product details")
    void should_return_200_when_getting_product_detail() throws Exception {
        ProductDetailResponse response = ProductDetailResponse.builder()
                .productId(productId)
                .productName("Aurora")
                .basePrice(new BigDecimal("89.00"))
                .originalPrice(new BigDecimal("130.00"))
                .piecesText("1 ly")
                .dietaryTags(List.of("Popular", "Sweet"))
                .isAvailable(true)
                .build();

        when(getProductDetailUseCase.execute(productId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Aurora"))
                .andExpect(jsonPath("$.data.basePrice").value(89.00))
                .andExpect(jsonPath("$.data.originalPrice").value(130.00));
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId}/modifiers - should return 200 with modifiers")
    void should_return_200_when_getting_modifiers() throws Exception {
        ModifierGroupResponse group = ModifierGroupResponse.builder()
                .id(UUID.randomUUID())
                .name("Size")
                .minSelection(1)
                .maxSelection(1)
                .displayOrder(1)
                .modifiers(List.of(
                        ModifierResponse.builder()
                                .id(UUID.randomUUID())
                                .name("L")
                                .extraPrice(new BigDecimal("15.00"))
                                .isDefault(true)
                                .build()
                ))
                .build();

        when(getProductModifiersUseCase.execute(productId)).thenReturn(List.of(group));

        mockMvc.perform(get("/api/v1/products/{productId}/modifiers", productId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Size"))
                .andExpect(jsonPath("$.data[0].modifiers[0].name").value("L"))
                .andExpect(jsonPath("$.data[0].modifiers[0].extraPrice").value(15.00));
    }

    @Test
    @DisplayName("GET /api/v1/products/{productId} - should return 404 when product not found")
    void should_return_404_when_product_not_found() throws Exception {
        when(getProductDetailUseCase.execute(productId))
                .thenThrow(new BusinessException(CatalogErrorCode.PRODUCT_NOT_FOUND, "Product does not exist."));

        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"));
    }
}
