package com.ordera.modules.order.presentation.controller;

import com.ordera.common.api.ApiResponse;
import com.ordera.common.context.TenantContextHolder;
import com.ordera.common.util.SecurityUtils;
import com.ordera.modules.order.application.service.CartService;
import com.ordera.modules.order.presentation.constant.OrderMessages;
import com.ordera.modules.order.presentation.dto.request.AddCartItemRequest;
import com.ordera.modules.order.presentation.dto.request.UpdateCartCommentRequest;
import com.ordera.modules.order.presentation.dto.request.UpdateCartItemQuantityRequest;
import com.ordera.modules.order.presentation.dto.response.CartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * Controller quản lý giỏ hàng khách hàng (APIs 6.1, 6.2, 6.3, 6.4).
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "Quản lý giỏ hàng tạm thời, thêm/sửa món, topping và ghi chú dị ứng")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "6.1: Lấy chi tiết giỏ hàng hiện tại (Get My Cart)")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(
            @RequestParam(required = false) UUID storeId,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        CartResponse response = cartService.getMyCart(tenantId, userId, storeId);
        String message = response.getTotalItemCount() > 0 ? OrderMessages.MSG_CART_RETRIEVED : OrderMessages.MSG_CART_EMPTY;
        return ResponseEntity.ok(ApiResponse.ok(message, response));
    }

    @PostMapping("/items")
    @Operation(summary = "6.2: Thêm món ăn vào giỏ kèm modifier (Add Item to Cart)")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody AddCartItemRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        CartResponse response = cartService.addItem(tenantId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(OrderMessages.MSG_ITEM_ADDED_TO_CART, response));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "6.3: Cập nhật số lượng món trong giỏ (Update Cart Item Quantity)")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        CartResponse response = cartService.updateItemQuantity(tenantId, userId, itemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.ok(OrderMessages.MSG_CART_ITEM_UPDATED, response));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "6.3: Xóa món ăn khỏi giỏ hàng (Delete Cart Item)")
    public ResponseEntity<ApiResponse<CartResponse>> deleteItem(
            @PathVariable UUID itemId,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        CartResponse response = cartService.deleteItem(tenantId, userId, itemId);
        return ResponseEntity.ok(ApiResponse.ok(OrderMessages.MSG_CART_ITEM_REMOVED, response));
    }

    @PutMapping("/comment")
    @Operation(summary = "6.4: Cập nhật ghi chú đơn hàng dị ứng (Update Cart Comment)")
    public ResponseEntity<ApiResponse<CartResponse>> updateComment(
            @Valid @RequestBody UpdateCartCommentRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {
        UUID tenantId = TenantContextHolder.getRequiredTenantId();
        UUID userId = SecurityUtils.resolveCurrentUserId(userIdHeader, principal);

        CartResponse response = cartService.updateComment(tenantId, userId, request.getComment());
        return ResponseEntity.ok(ApiResponse.ok(OrderMessages.MSG_ALLERGY_NOTE_UPDATED, response));
    }
}

