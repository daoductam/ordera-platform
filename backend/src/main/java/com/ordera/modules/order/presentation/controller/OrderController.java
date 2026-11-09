package com.ordera.modules.order.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.context.TenantContextHolder;
import com.ordera.common.exception.BusinessException;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.order.application.usecase.*;
import com.ordera.modules.order.domain.exception.OrderErrorCode;
import com.ordera.modules.order.presentation.constant.OrderMessages;
import com.ordera.modules.order.presentation.dto.request.CheckoutOrderRequest;
import com.ordera.modules.order.presentation.dto.request.PreviewCheckoutRequest;
import com.ordera.modules.order.presentation.dto.request.SendEReceiptEmailRequest;
import com.ordera.modules.order.presentation.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * Controller quản lý Đơn hàng, Checkout, Dự toán VAT/Tips, Đặt lại & Hóa đơn điện tử (APIs 6.5 - 6.10).
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order & Checkout", description = "Dự toán chi phí, chốt đơn thanh toán, lịch sử, chi tiết, reorder và E-Receipt")
public class OrderController {

    private final PreviewCheckoutCalculationUseCase previewCalculationUseCase;
    private final CheckoutOrderUseCase checkoutOrderUseCase;
    private final GetMyOrdersUseCase getMyOrdersUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final ReorderPastOrderUseCase reorderPastOrderUseCase;
    private final SendEReceiptEmailUseCase sendEReceiptEmailUseCase;

    @PostMapping("/checkout/preview")
    @Operation(summary = "6.5: Dự toán bảng tính Checkout (VAT 15%, Tips, Voucher, Điểm thưởng)")
    public ResponseEntity<ApiResponse<PreviewCheckoutResponse>> previewCheckout(
            @Valid @RequestBody PreviewCheckoutRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        PreviewCheckoutResponse response = previewCalculationUseCase.execute(tenantId, userId, request);
        return ResponseEntity.ok(ApiResponse.ok(OrderMessages.MSG_CHECKOUT_PREVIEW_CALCULATED, response));
    }

    @PostMapping("/checkout")
    @Operation(summary = "6.6: Khởi tạo đơn hàng chính thức & xử lý thanh toán (Checkout Order)")
    public ResponseEntity<ApiResponse<CheckoutOrderResponse>> checkoutOrder(
            @Valid @RequestBody CheckoutOrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        CheckoutOrderResponse response = checkoutOrderUseCase.execute(tenantId, userId, idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(OrderMessages.MSG_ORDER_CHECKOUT_SUCCESS, response));
    }

    @GetMapping
    @Operation(summary = "6.7: Danh sách đơn hàng phân chia theo tab ACTIVE / PAST (List My Orders)")
    public ResponseEntity<ApiResponse<com.ordera.common.api.PageResponse<OrderSummaryResponse>>> getMyOrders(
            @RequestParam(required = false, defaultValue = "ACTIVE") String tab,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        if (userId == null) {
            throw new BusinessException(OrderErrorCode.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderSummaryResponse> response = getMyOrdersUseCase.execute(tenantId, userId, tab, pageable);
        return ResponseEntity.ok(ApiResponse.ok(OrderMessages.MSG_ORDERS_LIST_RETRIEVED, com.ordera.common.api.PageResponse.from(response)));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "6.8: Chi tiết đơn hàng & timeline bếp (Get Order Details)")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        OrderDetailResponse response = getOrderDetailUseCase.execute(tenantId, userId, orderId);
        return ResponseEntity.ok(ApiResponse.ok(OrderMessages.MSG_ORDER_DETAIL_RETRIEVED, response));
    }

    @PostMapping("/{orderId}/reorder")
    @Operation(summary = "6.9: Đặt lại đơn hàng cũ nhanh chóng 1-click (Reorder Past Order)")
    public ResponseEntity<ApiResponse<ReorderResponse>> reorderPastOrder(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        if (userId == null) {
            throw new BusinessException(OrderErrorCode.UNAUTHORIZED);
        }

        ReorderResponse response = reorderPastOrderUseCase.execute(tenantId, userId, orderId);
        return ResponseEntity.ok(ApiResponse.ok(OrderMessages.MSG_REORDER_SUCCESS, response));
    }

    @PostMapping("/{orderId}/receipt/send-email")
    @Operation(summary = "6.10: Gửi biên nhận điện tử E-Receipt PDF qua Email (Send E-Receipt)")
    public ResponseEntity<ApiResponse<SendEReceiptResponse>> sendReceiptEmail(
            @PathVariable UUID orderId,
            @Valid @RequestBody SendEReceiptEmailRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        SendEReceiptResponse response = sendEReceiptEmailUseCase.execute(tenantId, userId, orderId, request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(OrderMessages.MSG_RECEIPT_EMAIL_SENT, response));
    }
}
