package com.ordera.modules.tenant.presentation;

import com.ordera.common.api.ApiResponse;
import com.ordera.modules.tenant.application.usecase.*;
import com.ordera.modules.tenant.domain.exception.UnauthorizedException;
import com.ordera.modules.tenant.domain.model.FulfillmentType;
import com.ordera.modules.tenant.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Tag(name = "01. Store", description = "Quản lý chi nhánh cửa hàng, bàn ăn & yêu thích")
public class StoreController {

    private final GetStoresListUseCase getStoresListUseCase;
    private final GetStoreDetailUseCase getStoreDetailUseCase;
    private final GetStoreTablesUseCase getStoreTablesUseCase;
    private final ResolveTableQrUseCase resolveTableQrUseCase;
    private final ToggleFavoriteStoreUseCase toggleFavoriteStoreUseCase;
    private final GetFavoriteStoresUseCase getFavoriteStoresUseCase;

    @Operation(summary = "Danh sách chi nhánh cửa hàng", description = "Tìm kiếm chi nhánh, tính khoảng cách GPS Haversine, lọc hình thức nhận")
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreSummaryResponse>>> listStores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String fulfillmentType,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {

        FulfillmentType parsedType;
        try {
            parsedType = FulfillmentType.valueOf(fulfillmentType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Giá trị 'fulfillmentType' không hợp lệ. Chỉ chấp nhận ALL, DINE_IN, TAKEAWAY, DELIVERY.");
        }

        UUID userId = resolveCurrentUserId(userIdHeader, principal);
        List<StoreSummaryResponse> stores = getStoresListUseCase.execute(keyword, parsedType, latitude, longitude, userId);
        return ResponseEntity.ok(ApiResponse.ok("Stores retrieved successfully", stores));
    }

    @Operation(summary = "Chi tiết một chi nhánh", description = "Lấy chi tiết cửa hàng theo ID")
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreById(@PathVariable UUID storeId) {
        StoreDetailResponse store = getStoreDetailUseCase.execute(storeId);
        return ResponseEntity.ok(ApiResponse.ok("Store details retrieved successfully", store));
    }

    @Operation(summary = "Danh sách bàn ăn của chi nhánh", description = "Lấy tất cả bàn ăn của chi nhánh chỉ định")
    @GetMapping("/{storeId}/tables")
    public ResponseEntity<ApiResponse<List<StoreTableResponse>>> getStoreTables(@PathVariable UUID storeId) {
        List<StoreTableResponse> tables = getStoreTablesUseCase.execute(storeId);
        return ResponseEntity.ok(ApiResponse.ok("Tables retrieved successfully", tables));
    }

    @Operation(summary = "Giải mã mã QR bàn ăn", description = "Quét mã QR bàn ăn giải mã số bàn và chi nhánh")
    @GetMapping("/tables/qr/{qrToken}")
    public ResponseEntity<ApiResponse<ResolvedTableQrResponse>> resolveTableByQrToken(@PathVariable String qrToken) {
        ResolvedTableQrResponse response = resolveTableQrUseCase.execute(qrToken);
        return ResponseEntity.ok(ApiResponse.ok("Table resolved successfully", response));
    }

    @Operation(summary = "Tùy chọn hình thức nhận hàng", description = "Lấy danh sách hình thức nhận món (Dine-in, Takeaway, Delivery) & khung giờ")
    @GetMapping("/fulfillment-options")
    public ResponseEntity<ApiResponse<FulfillmentOptionsResponse>> getFulfillmentOptions(
            @RequestParam(required = false) UUID storeId) {

        FulfillmentOptionsResponse response = FulfillmentOptionsResponse.builder()
                .supportedTypes(List.of(
                        FulfillmentOptionsResponse.SupportedTypeDto.builder()
                                .type("DINE_IN").label("Dine In").icon("utensils").enabled(true).build(),
                        FulfillmentOptionsResponse.SupportedTypeDto.builder()
                                .type("TAKEAWAY").label("Takeaway").icon("shopping-bag").enabled(true).build(),
                        FulfillmentOptionsResponse.SupportedTypeDto.builder()
                                .type("DELIVERY").label("Delivery").icon("bike").enabled(true).build()
                ))
                .timeOptions(FulfillmentOptionsResponse.TimeOptionsDto.builder()
                        .asap(FulfillmentOptionsResponse.AsapOptionDto.builder()
                                .label("As soon as possible").estimatedPreparingMinutes(15).build())
                        .schedule(FulfillmentOptionsResponse.ScheduleOptionDto.builder()
                                .label("Schedule")
                                .sublabel("Select a time")
                                .allowSameDayOnly(true)
                                .availableSlots(Arrays.asList(
                                        "11:30 - 12:00",
                                        "12:00 - 12:30",
                                        "12:30 - 13:00",
                                        "17:30 - 18:00",
                                        "18:00 - 18:30"
                                ))
                                .build())
                        .build())
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Fulfillment options retrieved successfully", response));
    }

    @Operation(summary = "Lưu chi nhánh vào danh sách yêu thích", description = "Bật trạng thái yêu thích chi nhánh")
    @PostMapping("/{storeId}/favorite")
    public ResponseEntity<ApiResponse<ToggleFavoriteResponse>> addFavoriteStore(
            @PathVariable UUID storeId,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {

        UUID userId = resolveCurrentUserId(userIdHeader, principal);
        if (userId == null) {
            throw new UnauthorizedException("Vui lòng đăng nhập để lưu chi nhánh vào danh sách yêu thích.");
        }

        ToggleFavoriteResponse response = toggleFavoriteStoreUseCase.execute(storeId, userId, true);
        return ResponseEntity.ok(ApiResponse.ok("Store added to favorites successfully", response));
    }

    @Operation(summary = "Bỏ lưu chi nhánh khỏi danh sách yêu thích", description = "Tắt trạng thái yêu thích chi nhánh")
    @DeleteMapping("/{storeId}/favorite")
    public ResponseEntity<ApiResponse<ToggleFavoriteResponse>> removeFavoriteStore(
            @PathVariable UUID storeId,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {

        UUID userId = resolveCurrentUserId(userIdHeader, principal);
        if (userId == null) {
            throw new UnauthorizedException("Vui lòng đăng nhập để gỡ chi nhánh khỏi danh sách yêu thích.");
        }

        ToggleFavoriteResponse response = toggleFavoriteStoreUseCase.execute(storeId, userId, false);
        return ResponseEntity.ok(ApiResponse.ok("Store removed from favorites successfully", response));
    }

    @Operation(summary = "Danh sách chi nhánh yêu thích của hội viên", description = "Lấy tất cả các chi nhánh hội viên đã đánh dấu yêu thích")
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<StoreSummaryResponse>>> getFavoriteStores(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestHeader(value = "X-User-ID", required = false) String userIdHeader,
            Principal principal) {

        UUID userId = resolveCurrentUserId(userIdHeader, principal);
        if (userId == null) {
            throw new UnauthorizedException("Vui lòng đăng nhập để xem danh sách yêu thích.");
        }

        List<StoreSummaryResponse> favorites = getFavoriteStoresUseCase.execute(userId, latitude, longitude);
        return ResponseEntity.ok(ApiResponse.ok("Favorite stores retrieved successfully", favorites));
    }

    private UUID resolveCurrentUserId(String userIdHeader, Principal principal) {
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                return UUID.fromString(userIdHeader.trim());
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (principal != null) {
            try {
                return UUID.fromString(principal.getName());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}

