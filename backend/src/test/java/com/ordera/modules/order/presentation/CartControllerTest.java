package com.ordera.modules.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.order.application.service.CartService;
import com.ordera.modules.order.presentation.constant.OrderMessages;
import com.ordera.modules.order.presentation.controller.CartController;
import com.ordera.modules.order.presentation.dto.request.AddCartItemRequest;
import com.ordera.modules.order.presentation.dto.request.UpdateCartCommentRequest;
import com.ordera.modules.order.presentation.dto.request.UpdateCartItemQuantityRequest;
import com.ordera.modules.order.presentation.dto.response.CartResponse;
import org.junit.jupiter.api.AfterEach;
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
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartController MockMvc Unit Tests")
class CartControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("GET /api/v1/cart should return 200 with MSG_CART_RETRIEVED")
    void should_get_cart_successfully() throws Exception {
        CartResponse cartResponse = CartResponse.builder()
                .cartId(UUID.randomUUID())
                .totalItemCount(1)
                .subtotalAmount(new BigDecimal("89.00"))
                .currency("NOK")
                .items(new ArrayList<>())
                .build();

        when(cartService.getMyCart(eq(tenantId), any(), any())).thenReturn(cartResponse);

        mockMvc.perform(get("/api/v1/cart")
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_CART_RETRIEVED))
                .andExpect(jsonPath("$.data.subtotalAmount").value(89.00));
    }

    @Test
    @DisplayName("POST /api/v1/cart/items should return 201 with MSG_ITEM_ADDED_TO_CART")
    void should_add_item_to_cart_successfully() throws Exception {
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId(UUID.randomUUID())
                .quantity(2)
                .build();

        CartResponse cartResponse = CartResponse.builder()
                .cartId(UUID.randomUUID())
                .totalItemCount(2)
                .subtotalAmount(new BigDecimal("178.00"))
                .currency("NOK")
                .build();

        when(cartService.addItem(eq(tenantId), any(), any())).thenReturn(cartResponse);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ITEM_ADDED_TO_CART))
                .andExpect(jsonPath("$.data.totalItemCount").value(2));
    }

    @Test
    @DisplayName("PATCH /api/v1/cart/items/{itemId} should return 200 with MSG_CART_ITEM_UPDATED")
    void should_update_item_quantity_successfully() throws Exception {
        UUID itemId = UUID.randomUUID();
        UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest(3);

        CartResponse cartResponse = CartResponse.builder()
                .cartId(UUID.randomUUID())
                .totalItemCount(3)
                .subtotalAmount(new BigDecimal("267.00"))
                .build();

        when(cartService.updateItemQuantity(eq(tenantId), any(), eq(itemId), eq(3))).thenReturn(cartResponse);

        mockMvc.perform(patch("/api/v1/cart/items/" + itemId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_CART_ITEM_UPDATED));
    }

    @Test
    @DisplayName("PUT /api/v1/cart/comment should return 200 with MSG_ALLERGY_NOTE_UPDATED")
    void should_update_comment_successfully() throws Exception {
        UpdateCartCommentRequest request = new UpdateCartCommentRequest("No wasabi please");

        CartResponse cartResponse = CartResponse.builder()
                .cartId(UUID.randomUUID())
                .comment("No wasabi please")
                .build();

        when(cartService.updateComment(eq(tenantId), any(), eq("No wasabi please"))).thenReturn(cartResponse);

        mockMvc.perform(put("/api/v1/cart/comment")
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ALLERGY_NOTE_UPDATED));
    }
}

