package com.ordera.modules.catalog.presentation;

import com.ordera.common.exception.BusinessException;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.catalog.application.usecase.GetCategoriesUseCase;
import com.ordera.modules.catalog.application.usecase.GetSubcategoriesUseCase;
import com.ordera.modules.catalog.domain.exception.CatalogErrorCode;
import com.ordera.modules.catalog.presentation.dto.CategoryResponse;
import com.ordera.modules.catalog.presentation.dto.SubcategoryResponse;
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
@DisplayName("CategoryController WebMvc Tests")
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetCategoriesUseCase getCategoriesUseCase;

    @Mock
    private GetSubcategoriesUseCase getSubcategoriesUseCase;

    @InjectMocks
    private CategoryController categoryController;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID storeId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId}/categories - should return 200 with list of categories")
    void should_return_200_when_getting_categories() throws Exception {
        CategoryResponse cat = CategoryResponse.builder()
                .id(categoryId)
                .name("Maki")
                .icon("sushi")
                .displayOrder(1)
                .subcategories(List.of(
                        SubcategoryResponse.builder()
                                .id(UUID.randomUUID())
                                .name("Fresh maki")
                                .displayOrder(1)
                                .build()
                ))
                .build();

        when(getCategoriesUseCase.execute(storeId)).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/v1/stores/{storeId}/categories", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Maki"))
                .andExpect(jsonPath("$.data[0].icon").value("sushi"))
                .andExpect(jsonPath("$.data[0].subcategories[0].name").value("Fresh maki"));
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId}/categories - should return 404 when store not found")
    void should_return_404_when_store_not_found() throws Exception {
        when(getCategoriesUseCase.execute(storeId))
                .thenThrow(new BusinessException(CatalogErrorCode.STORE_NOT_FOUND, "Store does not exist."));

        mockMvc.perform(get("/api/v1/stores/{storeId}/categories", storeId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("STORE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{categoryId}/subcategories - should return 200 with subcategories")
    void should_return_200_when_getting_subcategories() throws Exception {
        SubcategoryResponse sub = SubcategoryResponse.builder()
                .id(UUID.randomUUID())
                .name("Fried maki")
                .displayOrder(2)
                .build();

        when(getSubcategoriesUseCase.execute(categoryId)).thenReturn(List.of(sub));

        mockMvc.perform(get("/api/v1/categories/{categoryId}/subcategories", categoryId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Fried maki"));
    }
}
