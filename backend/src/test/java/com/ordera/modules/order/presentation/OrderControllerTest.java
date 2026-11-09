package com.ordera.modules.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.order.application.usecase.*;
import com.ordera.modules.order.presentation.constant.OrderMessages;
import com.ordera.modules.order.presentation.controller.OrderController;
import com.ordera.modules.order.presentation.dto.request.CheckoutOrderRequest;
import com.ordera.modules.order.presentation.dto.request.PreviewCheckoutRequest;
import com.ordera.modules.order.presentation.dto.request.SendEReceiptEmailRequest;
import com.ordera.modules.order.presentation.dto.response.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController MockMvc Unit Tests")
class OrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PreviewCheckoutCalculationUseCase previewCalculationUseCase;
    @Mock
    private CheckoutOrderUseCase checkoutOrderUseCase;
    @Mock
    private GetMyOrdersUseCase getMyOrdersUseCase;
    @Mock
    private GetOrderDetailUseCase getOrderDetailUseCase;
    @Mock
    private ReorderPastOrderUseCase reorderPastOrderUseCase;
    @Mock
    private SendEReceiptEmailUseCase sendEReceiptEmailUseCase;

    @InjectMocks
    private OrderController orderController;

    private UUID tenantId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
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
    @DisplayName("POST /api/v1/orders/checkout/preview should return 200 with MSG_CHECKOUT_PREVIEW_CALCULATED")
    void should_preview_checkout_successfully() throws Exception {
        PreviewCheckoutRequest request = PreviewCheckoutRequest.builder()
                .cartId(UUID.randomUUID())
                .voucherCode("DISCOUNT30")
                .tipAmount(new BigDecimal("10.00"))
                .build();

        PreviewCheckoutResponse response = PreviewCheckoutResponse.builder()
                .subtotalAmount(new BigDecimal("250.00"))
                .discountAmount(new BigDecimal("75.00"))
                .tipAmount(new BigDecimal("10.00"))
                .vatAmount(new BigDecimal("24.13"))
                .finalTotalAmount(new BigDecimal("185.00"))
                .currency("NOK")
                .pointsToEarn(175)
                .build();

        when(previewCalculationUseCase.execute(eq(tenantId), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/checkout/preview")
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_CHECKOUT_PREVIEW_CALCULATED))
                .andExpect(jsonPath("$.data.finalTotalAmount").value(185.00));
    }

    @Test
    @DisplayName("POST /api/v1/orders/checkout should return 201 with MSG_ORDER_CHECKOUT_SUCCESS")
    void should_checkout_order_successfully() throws Exception {
        CheckoutOrderRequest request = CheckoutOrderRequest.builder()
                .cartId(UUID.randomUUID())
                .pickupMethod("COUNTER")
                .pickupPersonName("David Nguyen")
                .pickupPersonPhone("+4797362748")
                .paymentMethod("VIPPS")
                .build();

        CheckoutOrderResponse response = CheckoutOrderResponse.builder()
                .orderId(UUID.randomUUID())
                .orderNumber("#005")
                .orderCode("ORD-20261105-0891")
                .pickupMethod("COUNTER")
                .totalAmount(new BigDecimal("185.00"))
                .pointsEarned(175)
                .status("PREPARING")
                .paymentStatus("PAID")
                .paymentMethod("VIPPS")
                .build();

        when(checkoutOrderUseCase.execute(eq(tenantId), any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString())
                        .header("Idempotency-Key", "idem-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ORDER_CHECKOUT_SUCCESS))
                .andExpect(jsonPath("$.data.orderNumber").value("#005"));
    }

    @Test
    @DisplayName("GET /api/v1/orders should return 200 with MSG_ORDERS_LIST_RETRIEVED")
    void should_get_my_orders_list() throws Exception {
        OrderSummaryResponse summary = OrderSummaryResponse.builder()
                .orderId(UUID.randomUUID())
                .orderNumber("#005")
                .orderCode("ORD-20261105-0891")
                .storeName("Linn Sushi Sentrum")
                .totalAmount(new BigDecimal("185.00"))
                .itemCount(2)
                .status("PREPARING")
                .build();

        when(getMyOrdersUseCase.execute(eq(tenantId), any(), eq("ACTIVE"), any()))
                .thenReturn(new PageImpl<>(List.of(summary)));

        mockMvc.perform(get("/api/v1/orders?tab=ACTIVE")
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ORDERS_LIST_RETRIEVED))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("#005"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderId} should return 200 with MSG_ORDER_DETAIL_RETRIEVED")
    void should_get_order_detail() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderDetailResponse response = OrderDetailResponse.builder()
                .orderId(orderId)
                .orderNumber("#005")
                .orderCode("ORD-20261105-0891")
                .status("PREPARING")
                .pickupMethod("COUNTER")
                .pointsEarned(170)
                .build();

        when(getOrderDetailUseCase.execute(tenantId, userId, orderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ORDER_DETAIL_RETRIEVED));
    }

    @Test
    @DisplayName("POST /api/v1/orders/{orderId}/reorder should return 200 with MSG_REORDER_SUCCESS")
    void should_reorder_past_order() throws Exception {
        UUID orderId = UUID.randomUUID();
        ReorderResponse response = ReorderResponse.builder()
                .cartId(UUID.randomUUID())
                .itemCount(3)
                .subtotalAmount(new BigDecimal("250.00"))
                .build();

        when(reorderPastOrderUseCase.execute(tenantId, userId, orderId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/reorder")
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_REORDER_SUCCESS));
    }

    @Test
    @DisplayName("POST /api/v1/orders/{orderId}/receipt/send-email should return 200 with MSG_RECEIPT_EMAIL_SENT")
    void should_send_receipt_email() throws Exception {
        UUID orderId = UUID.randomUUID();
        SendEReceiptEmailRequest request = new SendEReceiptEmailRequest("info@ordera.no");

        SendEReceiptResponse response = SendEReceiptResponse.builder()
                .sentToEmail("info@ordera.no")
                .orderNumber("#005")
                .dispatchedAt(Instant.now())
                .build();

        when(sendEReceiptEmailUseCase.execute(tenantId, userId, orderId, "info@ordera.no")).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/receipt/send-email")
                        .header("X-Tenant-ID", tenantId.toString())
                        .header("X-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_RECEIPT_EMAIL_SENT));
    }
}
