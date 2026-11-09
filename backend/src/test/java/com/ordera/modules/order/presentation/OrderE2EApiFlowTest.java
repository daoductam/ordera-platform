package com.ordera.modules.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordera.common.api.PageResponse;
import com.ordera.common.context.TenantContext;
import com.ordera.common.exception.GlobalExceptionHandler;
import com.ordera.modules.order.application.service.CartService;
import com.ordera.modules.order.application.usecase.*;
import com.ordera.modules.order.presentation.constant.OrderMessages;
import com.ordera.modules.order.presentation.controller.CartController;
import com.ordera.modules.order.presentation.controller.OrderController;
import com.ordera.modules.order.presentation.dto.request.*;
import com.ordera.modules.order.presentation.dto.response.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order End-to-End 10-API Customer Journey Simulation Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderE2EApiFlowTest {

    private MockMvc cartMockMvc;
    private MockMvc orderMockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CartService cartService;
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

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CART_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PRODUCT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID ITEM_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID ORDER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @BeforeEach
    void setUp() {
        CartController cartController = new CartController(cartService);
        OrderController orderController = new OrderController(
                previewCalculationUseCase,
                checkoutOrderUseCase,
                getMyOrdersUseCase,
                getOrderDetailUseCase,
                reorderPastOrderUseCase,
                sendEReceiptEmailUseCase
        );

        cartMockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        orderMockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        TenantContext.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @Order(1)
    @DisplayName("API 6.2 - Step 1: Add Salmon Nigiri to Cart")
    void step1_addItemToCart() throws Exception {
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId(PRODUCT_ID)
                .quantity(2)
                .selectedModifierIds(List.of(UUID.randomUUID()))
                .specialInstructions("Extra Wasabi")
                .build();

        CartResponse response = CartResponse.builder()
                .cartId(CART_ID)
                .storeName("Linn Sushi Sentrum")
                .totalItemCount(2)
                .subtotalAmount(new BigDecimal("150.00"))
                .currency("NOK")
                .items(List.of(
                        CartItemDetailResponse.builder()
                                .itemId(ITEM_ID)
                                .productId(PRODUCT_ID)
                                .productName("Salmon Nigiri 4 pcs")
                                .quantity(2)
                                .itemPrice(new BigDecimal("75.00"))
                                .subtotalPrice(new BigDecimal("150.00"))
                                .build()
                ))
                .build();

        when(cartService.addItem(eq(TENANT_ID), eq(USER_ID), any())).thenReturn(response);

        cartMockMvc.perform(post("/api/v1/cart/items")
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ITEM_ADDED_TO_CART))
                .andExpect(jsonPath("$.data.cartId").value(CART_ID.toString()))
                .andExpect(jsonPath("$.data.totalItemCount").value(2))
                .andExpect(jsonPath("$.data.subtotalAmount").value(150.00));
    }

    @Test
    @Order(2)
    @DisplayName("API 6.3 - Step 2: Increase Item Quantity to 3")
    void step2_updateItemQuantity() throws Exception {
        UpdateCartItemQuantityRequest request = new UpdateCartItemQuantityRequest(3);

        CartResponse response = CartResponse.builder()
                .cartId(CART_ID)
                .totalItemCount(3)
                .subtotalAmount(new BigDecimal("225.00"))
                .build();

        when(cartService.updateItemQuantity(eq(TENANT_ID), eq(USER_ID), eq(ITEM_ID), eq(3)))
                .thenReturn(response);

        cartMockMvc.perform(patch("/api/v1/cart/items/" + ITEM_ID)
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_CART_ITEM_UPDATED))
                .andExpect(jsonPath("$.data.totalItemCount").value(3))
                .andExpect(jsonPath("$.data.subtotalAmount").value(225.00));
    }

    @Test
    @Order(3)
    @DisplayName("API 6.4 - Step 3: Update Allergy Comment")
    void step3_updateAllergyComment() throws Exception {
        UpdateCartCommentRequest request = new UpdateCartCommentRequest("No gluten and extra ginger please");

        CartResponse response = CartResponse.builder()
                .cartId(CART_ID)
                .comment("No gluten and extra ginger please")
                .build();

        when(cartService.updateComment(eq(TENANT_ID), eq(USER_ID), eq("No gluten and extra ginger please")))
                .thenReturn(response);

        cartMockMvc.perform(put("/api/v1/cart/comment")
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ALLERGY_NOTE_UPDATED))
                .andExpect(jsonPath("$.data.comment").value("No gluten and extra ginger please"));
    }

    @Test
    @Order(4)
    @DisplayName("API 6.1 - Step 4: Fetch Active Cart")
    void step4_getMyCart() throws Exception {
        CartResponse response = CartResponse.builder()
                .cartId(CART_ID)
                .storeName("Linn Sushi Sentrum")
                .totalItemCount(3)
                .subtotalAmount(new BigDecimal("225.00"))
                .comment("No gluten and extra ginger please")
                .currency("NOK")
                .items(List.of(
                        CartItemDetailResponse.builder()
                                .itemId(ITEM_ID)
                                .productName("Salmon Nigiri 4 pcs")
                                .quantity(3)
                                .subtotalPrice(new BigDecimal("225.00"))
                                .build()
                ))
                .build();

        when(cartService.getMyCart(eq(TENANT_ID), eq(USER_ID), any())).thenReturn(response);

        cartMockMvc.perform(get("/api/v1/cart")
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_CART_RETRIEVED))
                .andExpect(jsonPath("$.data.cartId").value(CART_ID.toString()))
                .andExpect(jsonPath("$.data.subtotalAmount").value(225.00));
    }

    @Test
    @Order(5)
    @DisplayName("API 6.5 - Step 5: Preview Checkout Calculation with Voucher & Tip")
    void step5_previewCheckout() throws Exception {
        PreviewCheckoutRequest request = PreviewCheckoutRequest.builder()
                .cartId(CART_ID)
                .voucherCode("SAVE30")
                .tipAmount(new BigDecimal("10.00"))
                .build();

        PreviewCheckoutResponse response = PreviewCheckoutResponse.builder()
                .subtotalAmount(new BigDecimal("225.00"))
                .discountAmount(new BigDecimal("67.50"))
                .tipAmount(new BigDecimal("10.00"))
                .vatAmount(new BigDecimal("21.85"))
                .vatPercentage(15.0)
                .finalTotalAmount(new BigDecimal("167.50"))
                .currency("NOK")
                .pointsToEarn(157)
                .appliedVoucher(AppliedVoucherResponse.builder()
                        .code("SAVE30")
                        .title("-30% Sushi Deal")
                        .discountValueText("-30 %")
                        .discountAmount(new BigDecimal("67.50"))
                        .build())
                .build();

        when(previewCalculationUseCase.execute(eq(TENANT_ID), eq(USER_ID), any())).thenReturn(response);

        orderMockMvc.perform(post("/api/v1/orders/checkout/preview")
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_CHECKOUT_PREVIEW_CALCULATED))
                .andExpect(jsonPath("$.data.subtotalAmount").value(225.00))
                .andExpect(jsonPath("$.data.discountAmount").value(67.50))
                .andExpect(jsonPath("$.data.tipAmount").value(10.00))
                .andExpect(jsonPath("$.data.vatAmount").value(21.85))
                .andExpect(jsonPath("$.data.finalTotalAmount").value(167.50))
                .andExpect(jsonPath("$.data.pointsToEarn").value(157));
    }

    @Test
    @Order(6)
    @DisplayName("API 6.6 - Step 6: Checkout Order with Idempotency Key & VIPPS")
    void step6_checkoutOrder() throws Exception {
        CheckoutOrderRequest request = CheckoutOrderRequest.builder()
                .cartId(CART_ID)
                .pickupMethod("COUNTER")
                .pickupPersonName("David Nguyen")
                .pickupPersonPhone("+4797362748")
                .paymentMethod("VIPPS")
                .voucherCode("SAVE30")
                .tipAmount(new BigDecimal("10.00"))
                .eReceiptEmail("david.nguyen@ordera.no")
                .build();

        CheckoutOrderResponse response = CheckoutOrderResponse.builder()
                .orderId(ORDER_ID)
                .orderNumber("#005")
                .orderCode("ORD-20261109-0891")
                .pickupMethod("COUNTER")
                .totalAmount(new BigDecimal("167.50"))
                .pointsEarned(157)
                .status("PREPARING")
                .estimatedReadyMinutes(10)
                .paymentStatus("PAID")
                .paymentMethod("VIPPS")
                .createdAt(Instant.now())
                .build();

        when(checkoutOrderUseCase.execute(eq(TENANT_ID), eq(USER_ID), eq("idem-key-8899"), any()))
                .thenReturn(response);

        orderMockMvc.perform(post("/api/v1/orders/checkout")
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString())
                        .header("Idempotency-Key", "idem-key-8899")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ORDER_CHECKOUT_SUCCESS))
                .andExpect(jsonPath("$.data.orderNumber").value("#005"))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-20261109-0891"))
                .andExpect(jsonPath("$.data.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.data.totalAmount").value(167.50))
                .andExpect(jsonPath("$.data.pointsEarned").value(157));
    }

    @Test
    @Order(7)
    @DisplayName("API 6.7 - Step 7: Query Active Orders Tab")
    void step7_listActiveOrders() throws Exception {
        OrderSummaryResponse summary = OrderSummaryResponse.builder()
                .orderId(ORDER_ID)
                .orderNumber("#005")
                .orderCode("ORD-20261109-0891")
                .storeName("Linn Sushi Sentrum")
                .totalAmount(new BigDecimal("167.50"))
                .itemCount(3)
                .firstItemName("Salmon Nigiri 4 pcs")
                .status("PREPARING")
                .estimatedReadyMinutes(10)
                .createdAt(Instant.now())
                .build();

        when(getMyOrdersUseCase.execute(eq(TENANT_ID), eq(USER_ID), eq("ACTIVE"), any()))
                .thenReturn(new PageImpl<>(List.of(summary)));

        orderMockMvc.perform(get("/api/v1/orders?tab=ACTIVE&page=0&size=10")
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ORDERS_LIST_RETRIEVED))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("#005"))
                .andExpect(jsonPath("$.data.content[0].status").value("PREPARING"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @Order(8)
    @DisplayName("API 6.8 - Step 8: View Order Details & 3-Step Kitchen Timeline")
    void step8_getOrderDetailAndTimeline() throws Exception {
        OrderDetailResponse response = OrderDetailResponse.builder()
                .orderId(ORDER_ID)
                .orderNumber("#005")
                .orderCode("ORD-20261109-0891")
                .status("PREPARING")
                .pickupMethod("COUNTER")
                .estimatedReadyMinutes(10)
                .pointsEarned(157)
                .store(StoreInfoResponse.builder()
                        .name("Linn Sushi Sentrum")
                        .address("Prinsens gate 10C, 0153 Oslo")
                        .phoneNumber("+4722334455")
                        .build())
                .timeline(List.of(
                        new TimelineStepResponse("PLACED", "20:45", true),
                        new TimelineStepResponse("IN_KITCHEN", "20:45", true),
                        new TimelineStepResponse("READY_FOR_PICKUP", "20:45", false)
                ))
                .items(List.of(
                        OrderDetailItemResponse.builder()
                                .productName("Salmon Nigiri 4 pcs")
                                .quantity(3)
                                .unitPrice(new BigDecimal("75.00"))
                                .modifiersSummary("Option: Extra Wasabi")
                                .build()
                ))
                .financialSummary(FinancialSummaryResponse.builder()
                        .subtotalAmount(new BigDecimal("225.00"))
                        .discountAmount(new BigDecimal("67.50"))
                        .tipAmount(new BigDecimal("10.00"))
                        .vatAmount(new BigDecimal("21.85"))
                        .totalAmount(new BigDecimal("167.50"))
                        .paymentMethod("VIPPS")
                        .paymentStatus("PAID")
                        .build())
                .build();

        when(getOrderDetailUseCase.execute(TENANT_ID, USER_ID, ORDER_ID)).thenReturn(response);

        orderMockMvc.perform(get("/api/v1/orders/" + ORDER_ID)
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_ORDER_DETAIL_RETRIEVED))
                .andExpect(jsonPath("$.data.orderNumber").value("#005"))
                .andExpect(jsonPath("$.data.store.name").value("Linn Sushi Sentrum"))
                .andExpect(jsonPath("$.data.timeline[0].step").value("PLACED"))
                .andExpect(jsonPath("$.data.timeline[0].isDone").value(true))
                .andExpect(jsonPath("$.data.timeline[2].step").value("READY_FOR_PICKUP"))
                .andExpect(jsonPath("$.data.timeline[2].isDone").value(false))
                .andExpect(jsonPath("$.data.financialSummary.totalAmount").value(167.50));
    }

    @Test
    @Order(9)
    @DisplayName("API 6.9 - Step 9: 1-Click Reorder Past Order")
    void step9_reorderPastOrder() throws Exception {
        ReorderResponse response = ReorderResponse.builder()
                .cartId(CART_ID)
                .itemCount(3)
                .subtotalAmount(new BigDecimal("225.00"))
                .build();

        when(reorderPastOrderUseCase.execute(TENANT_ID, USER_ID, ORDER_ID)).thenReturn(response);

        orderMockMvc.perform(post("/api/v1/orders/" + ORDER_ID + "/reorder")
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_REORDER_SUCCESS))
                .andExpect(jsonPath("$.data.cartId").value(CART_ID.toString()))
                .andExpect(jsonPath("$.data.itemCount").value(3))
                .andExpect(jsonPath("$.data.subtotalAmount").value(225.00));
    }

    @Test
    @Order(10)
    @DisplayName("API 6.10 - Step 10: Dispatch E-Receipt PDF via Email")
    void step10_dispatchEReceiptEmail() throws Exception {
        SendEReceiptEmailRequest request = new SendEReceiptEmailRequest("david.nguyen@ordera.no");

        SendEReceiptResponse response = SendEReceiptResponse.builder()
                .sentToEmail("david.nguyen@ordera.no")
                .orderNumber("#005")
                .dispatchedAt(Instant.now())
                .build();

        when(sendEReceiptEmailUseCase.execute(TENANT_ID, USER_ID, ORDER_ID, "david.nguyen@ordera.no"))
                .thenReturn(response);

        orderMockMvc.perform(post("/api/v1/orders/" + ORDER_ID + "/receipt/send-email")
                        .header("X-Tenant-ID", TENANT_ID.toString())
                        .header("X-User-ID", USER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(OrderMessages.MSG_RECEIPT_EMAIL_SENT))
                .andExpect(jsonPath("$.data.sentToEmail").value("david.nguyen@ordera.no"))
                .andExpect(jsonPath("$.data.orderNumber").value("#005"))
                .andExpect(jsonPath("$.data.dispatchedAt").isNotEmpty());
    }
}
