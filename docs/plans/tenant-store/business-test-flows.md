# Sổ Tay Luồng Nghiệp Vụ & Kiểm Thử: Phân Hệ Tenant & Store
## Business & Test Flows Guide (Clean Architecture & Mermaid Visualizations)

> **Dành cho:** Thành viên mới (Developer, QA/Tester, Solution Architect)  
> **Mục đích:** Giúp người mới nắm bắt toàn bộ luồng nghiệp vụ, thứ tự tương tác giữa các tầng Clean Architecture và toàn bộ 65 ca kiểm thử biên (Edge Cases / Exception Scenarios) một cách trực quan trong vòng 5–10 phút mà không cần phải đọc từng dòng mã nguồn.

---

## 📑 Mục Lục
1. [Tổng Quan Kiến Trúc & Bảng Mã Lỗi (Quick Reference)](#1-tổng-quan-kiến-trúc--bảng-mã-lỗi)
2. [Use Case 1: Lấy Cấu hình Thương hiệu Hiện tại (`GetTenantInfoUseCase`)](#use-case-1-lấy-cấu-hình-thương-hiệu-hiện-tại-gettenantinfousecase)
3. [Use Case 2: Tìm kiếm & Sắp xếp Chi nhánh theo GPS (`GetStoresListUseCase`)](#use-case-2-tìm-kiếm--sắp-xếp-chi-nhánh-theo-gps-getstoreslistusecase)
4. [Use Case 3: Xem Chi tiết một Chi nhánh Cửa hàng (`GetStoreDetailUseCase`)](#use-case-3-xem-chi-tiết-một-chi-nhánh-cửa-hàng-getstoredetailusecase)
5. [Use Case 4: Lấy Danh sách Bàn ăn tại Chi nhánh (`GetStoreTablesUseCase`)](#use-case-4-lấy-danh-sách-bàn-ăn-tại-chi-nhánh-getstoretablesusecase)
6. [Use Case 5: Quét & Phân giải Mã QR Bàn ăn 3 Tầng (`ResolveTableQrUseCase`)](#use-case-5-quét--phân-giải-mã-qr-bàn-ăn-3-tầng-resolvetableqrusecase)
7. [Use Case 6: Tùy chọn Nhận hàng & Tự động sinh Khung giờ (`GetFulfillmentOptionsUseCase`)](#use-case-6-tùy-chọn-nhận-hàng--tự-động-sinh-khung-giờ-getfulfillmentoptionsusecase)
8. [Use Case 7: Lưu & Bỏ Chi nhánh Yêu thích (`ToggleFavoriteStoreUseCase`)](#use-case-7-lưu--bỏ-chi-nhánh-yêu-thích-togglefavoritestoreusecase)
9. [Use Case 8: Danh sách Chi nhánh Yêu thích của Hội viên (`GetFavoriteStoresUseCase`)](#use-case-8-danh-sách-chi-nhánh-yêu-thích-của-hội-viên-getfavoritestoresusecase)

---

## 1. Tổng Quan Kiến Trúc & Bảng Mã Lỗi

### Mô hình Luồng 4 Lớp Clean Architecture
```mermaid
flowchart LR
    Client(["Client (PWA / Kiosk)"]) -->|HTTP Request| Pre["Presentation: Controller & Interceptor"]
    Pre -->|Invoke Command| App["Application: Use Cases & DTOs"]
    App -->|Pure Logic| Dom["Domain: Entities & ErrorCode"]
    App -->|Pure Util| Util["Common Util: Geo, Qr, Security, Time"]
    App -->|Data Access| Infra["Infrastructure: Repositories & Mappers"]
    Infra -->|SQL / Cache| DB[("PostgreSQL & Redis")]

    classDef c1 fill:#e0f2fe,stroke:#0284c7,stroke-width:2px;
    classDef c2 fill:#fef3c7,stroke:#d97706,stroke-width:2px;
    classDef c3 fill:#dcfce7,stroke:#16a34a,stroke-width:2px;
    class Pre,App c1;
    class Dom,Util c2;
    class Infra,DB c3;
```

### Bảng Mã Lỗi Chuẩn (TenantErrorCode)
| HTTP Status | ErrorCode Enum | Ý nghĩa nghiệp vụ |
| :---: | :--- | :--- |
| `400` | `TENANT_HEADER_MISSING` | Thiếu hoặc rỗng header định danh thương hiệu `X-Tenant-ID`. |
| `400` | `INVALID_QUERY_PARAM` | Tham số query sai định dạng (ví dụ: `fulfillmentType` không thuộc ALL, DINE_IN, TAKEAWAY, DELIVERY). |
| `400` | `INVALID_QR_TOKEN` | Mã token QR bàn ăn bị rỗng hoặc không phân giải được ra bàn nào trong chi nhánh. |
| `401` | `UNAUTHORIZED` | Yêu cầu đăng nhập hội viên (thiếu `X-User-ID` hoặc Spring Security `Principal`). |
| `404` | `TENANT_NOT_FOUND` | ID thương hiệu trong header không tồn tại trong cơ sở dữ liệu. |
| `404` | `STORE_NOT_FOUND` | Cửa hàng không tồn tại hoặc không thuộc quyền sở hữu của thương hiệu hiện tại. |
| `404` | `TABLE_NOT_FOUND` | Bàn ăn chỉ định không tồn tại. |

---

## Use Case 1: Lấy Cấu hình Thương hiệu Hiện tại (GetTenantInfoUseCase)

- **API:** `GET /api/v1/tenants/current`
- **Use Case:** `GetTenantInfoUseCase`
- **Test File:** `GetTenantInfoUseCaseTest.java`, `TenantControllerTest.java`

### Biểu đồ Luồng Quyết Định (Decision Tree & Test Cases)
```mermaid
flowchart TD
    Start(["Client gọi GET /api/v1/tenants/current"]) --> CheckHdr{"X-Tenant-ID hợp lệ?"}
    CheckHdr -- Không --> ErrHdr["❌ 400 TENANT_HEADER_MISSING<br>test: should_throw_TENANT_HEADER_MISSING_when_tenant_is_null"]
    CheckHdr -- Có --> QueryDB["Truy vấn bảng tenants theo ID"]
    QueryDB --> CheckExist{"Tìm thấy Tenant?"}
    CheckExist -- Không --> ErrNotFound["❌ 404 TENANT_NOT_FOUND<br>test: should_throw_TENANT_NOT_FOUND_when_tenant_not_in_db"]
    CheckExist -- Có --> MapDTO["TenantDtoMapper trích xuất metadata slogan, vat, currency"]
    MapDTO --> Success(["✅ 200 OK - TenantResponse<br>test: should_return_tenant_details_when_tenant_exists"])

    classDef ok fill:#dcfce7,stroke:#16a34a,stroke-width:2px;
    classDef fail fill:#fee2e2,stroke:#dc2626,stroke-width:2px;
    class Success ok;
    class ErrHdr,ErrNotFound fail;
```

### Biểu đồ Trình Tự (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as TenantController
    participant UC as GetTenantInfoUseCase
    participant Repo as TenantRepository
    participant DB as PostgreSQL
    participant Map as TenantDtoMapper

    C->>Ctrl: GET /api/v1/tenants/current (X-Tenant-ID)
    Ctrl->>UC: execute()
    UC->>UC: TenantContext.getTenantId()
    alt Tenant ID == null
        UC-->>Ctrl: throw BusinessException(TENANT_HEADER_MISSING)
        Ctrl-->>C: 400 Bad Request
    end
    UC->>Repo: findById(tenantId)
    Repo->>DB: SELECT * FROM tenants WHERE id = ?
    DB-->>Repo: TenantJpaEntity / Optional.empty
    alt Tenant not found
        UC-->>Ctrl: throw BusinessException(TENANT_NOT_FOUND)
        Ctrl-->>C: 404 Not Found
    end
    UC->>Map: toTenantResponse(entity)
    Map-->>UC: TenantResponse
    UC-->>Ctrl: TenantResponse
    Ctrl-->>C: 200 OK (ApiResponse<TenantResponse>)
```

---

## Use Case 2: Tìm kiếm & Sắp xếp Chi nhánh theo GPS (GetStoresListUseCase)

- **API:** `GET /api/v1/stores?keyword=...&fulfillmentType=...&latitude=...&longitude=...`
- **Use Case:** `GetStoresListUseCase`
- **Util:** `GeoUtils.calculateDistanceKm`
- **Test File:** `GetStoresListUseCaseTest.java`, `StoreControllerTest.java`

### Biểu đồ Luồng Quyết Định (Decision Tree & Test Cases)
```mermaid
flowchart TD
    Start(["Client gọi GET /api/v1/stores"]) --> CheckType{"Enum fulfillmentType hợp lệ?"}
    CheckType -- Không --> ErrParam["❌ 400 INVALID_QUERY_PARAM<br>test: should_return_400_when_fulfillment_type_invalid"]
    CheckType -- Có --> CheckTenant{"X-Tenant-ID hợp lệ?"}
    CheckTenant -- Không --> ErrHdr["❌ 400 TENANT_HEADER_MISSING<br>test: should_throw_TENANT_HEADER_MISSING_when_tenant_is_null"]
    CheckTenant -- Có --> Search["SearchStores theo keyword nullable"]
    Search --> FilterType["Lọc danh sách theo DINE_IN, TAKEAWAY hoặc DELIVERY"]
    FilterType --> CheckUser{"Khách đã đăng nhập?"}
    CheckUser -- Có --> LoadFavs["Lấy tập hợp Store ID yêu thích của user"]
    CheckUser -- Không --> EmptyFavs["Tập hợp yêu thích rỗng"]
    LoadFavs --> CalcGPS["Duyệt danh sách: Gọi GeoUtils tính khoảng cách Haversine"]
    EmptyFavs --> CalcGPS
    CalcGPS --> SortGPS{"Có tọa độ GPS?"}
    SortGPS -- Có --> DoSort["Sắp xếp tăng dần theo khoảng cách gần nhất lên đầu"]
    SortGPS -- Không --> KeepOrder["Giữ nguyên thứ tự truy vấn"]
    DoSort --> Success(["✅ 200 OK - Danh sách StoreSummaryResponse<br>test: should_filter_by_fulfillment_type_and_sort_by_distance_ascending"])
    KeepOrder --> Success

    classDef ok fill:#dcfce7,stroke:#16a34a,stroke-width:2px;
    classDef fail fill:#fee2e2,stroke:#dc2626,stroke-width:2px;
    class Success ok;
    class ErrParam,ErrHdr fail;
```

### Biểu đồ Trình Tự (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as StoreController
    participant UC as GetStoresListUseCase
    participant SRepo as StoreRepository
    participant FRepo as CustomerFavoriteStoreRepository
    participant Geo as GeoUtils
    participant Map as TenantDtoMapper

    C->>Ctrl: GET /api/v1/stores (lat, lng, fulfillment, keyword)
    Ctrl->>Ctrl: Parse FulfillmentType & Resolve User ID
    Ctrl->>UC: execute(keyword, type, lat, lng, userId)
    UC->>SRepo: searchStores(tenantId, trimmedKeyword)
    SRepo-->>UC: List<StoreJpaEntity>
    UC->>UC: filterByFulfillment(stores, type)
    opt userId != null
        UC->>FRepo: findByTenantIdAndUserId(tenantId, userId)
        FRepo-->>UC: List<CustomerFavoriteStoreJpaEntity>
    end
    loop Mỗi Store
        opt Tọa độ GPS hợp lệ
            UC->>Geo: calculateDistanceKm(lat1, lon1, lat2, lon2)
            Geo-->>UC: distanceKm
        end
        UC->>Map: toStoreSummaryResponse(store, distanceKm, isFav)
        Map-->>UC: StoreSummaryResponse
    end
    UC->>UC: sortByDistanceIfAvailable()
    UC-->>Ctrl: List<StoreSummaryResponse>
    Ctrl-->>C: 200 OK
```

---

## Use Case 3: Xem Chi tiết một Chi nhánh Cửa hàng (GetStoreDetailUseCase)

- **API:** `GET /api/v1/stores/{storeId}`
- **Use Case:** `GetStoreDetailUseCase`
- **Test File:** `GetStoreDetailUseCaseTest.java`, `StoreControllerTest.java`

### Biểu đồ Luồng Quyết Định (Decision Tree & Test Cases)
```mermaid
flowchart TD
    Start(["Client gọi GET /api/v1/stores/{storeId}"]) --> CheckTenant{"X-Tenant-ID hợp lệ?"}
    CheckTenant -- Không --> ErrHdr["❌ 400 TENANT_HEADER_MISSING<br>test: should_throw_when_tenant_is_null"]
    CheckTenant -- Có --> FindStore["Truy vấn Store theo storeId & tenantId"]
    FindStore --> Exist{"Chi nhánh tồn tại?"}
    Exist -- Không --> ErrStore["❌ 404 STORE_NOT_FOUND<br>test: should_throw_when_store_does_not_exist"]
    Exist -- Có --> MapDetail["TenantDtoMapper ánh xạ StoreDetailResponse"]
    MapDetail --> Success(["✅ 200 OK - Chi tiết giờ mở cửa, ảnh bìa, logo, bán kính ship<br>test: should_return_store_detail_when_store_exists"])

    classDef ok fill:#dcfce7,stroke:#16a34a,stroke-width:2px;
    classDef fail fill:#fee2e2,stroke:#dc2626,stroke-width:2px;
    class Success ok;
    class ErrHdr,ErrStore fail;
```

### Biểu đồ Trình Tự (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as StoreController
    participant UC as GetStoreDetailUseCase
    participant SRepo as StoreRepository
    participant Map as TenantDtoMapper

    C->>Ctrl: GET /api/v1/stores/{storeId} (X-Tenant-ID)
    Ctrl->>UC: execute(storeId)
    UC->>UC: TenantContext.getTenantId()
    alt Tenant ID == null
        UC-->>Ctrl: throw BusinessException(TENANT_HEADER_MISSING)
        Ctrl-->>C: 400 Bad Request
    end
    UC->>SRepo: findByIdAndTenantId(storeId, tenantId)
    alt Store không tồn tại
        UC-->>Ctrl: throw BusinessException(STORE_NOT_FOUND)
        Ctrl-->>C: 404 Not Found
    end
    UC->>Map: toStoreDetailResponse(store)
    Map-->>UC: StoreDetailResponse
    UC-->>Ctrl: StoreDetailResponse
    Ctrl-->>C: 200 OK (ApiResponse<StoreDetailResponse>)
```

---

## Use Case 4: Lấy Danh sách Bàn ăn tại Chi nhánh (GetStoreTablesUseCase)

- **API:** `GET /api/v1/stores/{storeId}/tables`
- **Use Case:** `GetStoreTablesUseCase`
- **Test File:** `GetStoreTablesUseCaseTest.java`, `StoreControllerTest.java`

### Biểu đồ Luồng Quyết Định (Decision Tree & Test Cases)
```mermaid
flowchart TD
    Start(["Client gọi GET /api/v1/stores/{storeId}/tables"]) --> CheckTenant{"X-Tenant-ID hợp lệ?"}
    CheckTenant -- Không --> ErrHdr["❌ 400 TENANT_HEADER_MISSING<br>test: should_throw_when_tenant_is_null"]
    CheckTenant -- Có --> CheckStore["Xác thực Store thuộc Tenant"]
    CheckStore --> StoreOK{"Cửa hàng tồn tại?"}
    StoreOK -- Không --> ErrStore["❌ 404 STORE_NOT_FOUND<br>test: should_throw_when_store_does_not_exist"]
    StoreOK -- Có --> QueryTables["Truy vấn bảng store_tables theo tenantId & storeId"]
    QueryTables --> MapTables["Ánh xạ List StoreTableResponse"]
    MapTables --> Success(["✅ 200 OK - Danh sách bàn ăn, sức chứa, số bàn, khu vực<br>test: should_return_tables_list_when_store_exists"])

    classDef ok fill:#dcfce7,stroke:#16a34a,stroke-width:2px;
    classDef fail fill:#fee2e2,stroke:#dc2626,stroke-width:2px;
    class Success ok;
    class ErrHdr,ErrStore fail;
```

### Biểu đồ Trình Tự (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as StoreController
    participant UC as GetStoreTablesUseCase
    participant SRepo as StoreRepository
    participant TRepo as StoreTableRepository
    participant Map as TenantDtoMapper

    C->>Ctrl: GET /api/v1/stores/{storeId}/tables (X-Tenant-ID)
    Ctrl->>UC: execute(storeId)
    UC->>UC: TenantContext.getTenantId()
    alt Tenant ID == null
        UC-->>Ctrl: throw BusinessException(TENANT_HEADER_MISSING)
        Ctrl-->>C: 400 Bad Request
    end
    UC->>SRepo: existsByIdAndTenantId(storeId, tenantId)
    alt Store không tồn tại
        UC-->>Ctrl: throw BusinessException(STORE_NOT_FOUND)
        Ctrl-->>C: 404 Not Found
    end
    UC->>TRepo: findByTenantIdAndStoreIdOrderByTableNumberAsc(tenantId, storeId)
    TRepo-->>UC: List<StoreTableJpaEntity>
    UC->>Map: toStoreTableResponse(table)
    Map-->>UC: StoreTableResponse
    UC-->>Ctrl: List<StoreTableResponse>
    Ctrl-->>C: 200 OK (ApiResponse<List<StoreTableResponse>>)
```

---

## Use Case 5: Quét & Phân giải Mã QR Bàn ăn 3 Tầng (ResolveTableQrUseCase)

- **API:** `GET /api/v1/stores/tables/qr/{qrToken}`
- **Use Case:** `ResolveTableQrUseCase`
- **Util:** `QrCodeUtils`
- **Test File:** `ResolveTableQrUseCaseTest.java`, `StoreControllerTest.java`

### Biểu đồ Luồng Quyết Định (Decision Tree & Test Cases)
```mermaid
flowchart TD
    Start(["Khách quét mã QR trên mặt bàn"]) --> CheckTenant{"X-Tenant-ID hợp lệ?"}
    CheckTenant -- Không --> ErrHdr["❌ 400 TENANT_HEADER_MISSING<br>test: should_throw_when_tenant_is_null"]
    CheckTenant -- Có --> CheckBlank{"Token rỗng hoặc blank?"}
    CheckBlank -- Có --> ErrBlank["❌ 400 INVALID_QR_TOKEN<br>test: should_throw_when_token_is_blank"]
    CheckBlank -- Không --> Tier1["Tầng 1: Khớp chính xác Token không phân biệt hoa thường"]
    
    Tier1 --> Match1{"Tìm thấy bàn?"}
    Match1 -- Có --> CheckStore["Kiểm tra Cửa hàng sở hữu bàn"]
    Match1 -- Không --> Tier2["Tầng 2: Chuẩn hóa dấu gạch nối sang gạch dưới"]
    
    Tier2 --> Match2{"Tìm thấy bàn?"}
    Match2 -- Có --> CheckStore
    Match2 -- Không --> Tier3["Tầng 3: QrCodeUtils Regex trích xuất số bàn (vd: QR-LINN-SENTRUM-T05 => Table 05)"]
    
    Tier3 --> Match3{"Tìm thấy bàn?"}
    Match3 -- Có --> CheckStore
    Match3 -- Không --> ErrInvalid["❌ 400 INVALID_QR_TOKEN<br>test: should_throw_when_qr_token_is_invalid"]
    
    CheckStore --> StoreOK{"Cửa hàng tồn tại trong Tenant?"}
    StoreOK -- Không --> ErrStore["❌ 404 STORE_NOT_FOUND<br>test: should_throw_STORE_NOT_FOUND_when_store_not_in_tenant"]
    StoreOK -- Có --> Success(["✅ 200 OK - ResolvedTableQrResponse<br>test: should_resolve_table_by_fallback_regex_pattern"])

    classDef ok fill:#dcfce7,stroke:#16a34a,stroke-width:2px;
    classDef fail fill:#fee2e2,stroke:#dc2626,stroke-width:2px;
    class Success ok;
    class ErrHdr,ErrBlank,ErrInvalid,ErrStore fail;
```

### Biểu đồ Trình Tự Giải Mã Đa Tầng (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client (PWA / Kiosk)
    participant Ctrl as StoreController
    participant UC as ResolveTableQrUseCase
    participant Qr as QrCodeUtils
    participant TRepo as StoreTableRepository
    participant SRepo as StoreRepository

    C->>Ctrl: GET /api/v1/stores/tables/qr/{qrToken}
    Ctrl->>UC: execute(qrToken)
    UC->>TRepo: findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, rawToken)
    alt Không tìm thấy Tầng 1
        UC->>Qr: normalizeHyphenToUnderscore(rawToken)
        Qr-->>UC: underscoreToken
        UC->>TRepo: findByTenantIdAndQrCodeTokenIgnoreCase(tenantId, underscoreToken)
    end
    alt Vẫn không tìm thấy Tầng 2
        UC->>Qr: extractTableNumber(rawToken)
        Qr-->>UC: Optional<Integer> (ví dụ: 5)
        UC->>Qr: formatTableNumber(5, true/false)
        Qr-->>UC: "Table 05", "Table 5"
        UC->>TRepo: findAll() -> filter by tableNumber & tenantId
    end
    alt Không giải mã được qua cả 3 tầng
        UC-->>Ctrl: throw BusinessException(INVALID_QR_TOKEN)
        Ctrl-->>C: 400 Bad Request
    end
    UC->>SRepo: findByIdAndTenantId(table.storeId, tenantId)
    alt Store không tồn tại
        UC-->>Ctrl: throw BusinessException(STORE_NOT_FOUND)
        Ctrl-->>C: 404 Not Found
    end
    UC-->>Ctrl: ResolvedTableQrResponse
    Ctrl-->>C: 200 OK (storeName, tableNumber, zone, capacity)
```

---

## Use Case 6: Tùy chọn Nhận hàng & Tự động sinh Khung giờ (GetFulfillmentOptionsUseCase)

- **API:** `GET /api/v1/stores/fulfillment-options?storeId=...`
- **Use Case:** `GetFulfillmentOptionsUseCase`
- **Util:** `DateTimeUtils.generateTimeSlots`
- **Test File:** `GetFulfillmentOptionsUseCaseTest.java`, `StoreControllerTest.java`

### Biểu đồ Luồng Quyết Định (Decision Tree & Test Cases)
```mermaid
flowchart TD
    Start(["Client gọi GET /api/v1/stores/fulfillment-options"]) --> CheckTenant{"X-Tenant-ID hợp lệ?"}
    CheckTenant -- Không --> ErrHdr["❌ 400 TENANT_HEADER_MISSING<br>test: should_throw_when_tenant_is_null"]
    CheckTenant -- Có --> CheckStoreId{"Có truyền tham số storeId?"}
    
    CheckStoreId -- Không --> DefaultTenant["Trả về cấu hình mặc định toàn thương hiệu: Bật cả 3 loại, prepTime=15p, slots mặc định"]
    CheckStoreId -- Có --> QueryStore["Truy vấn Store theo storeId & tenantId"]
    
    QueryStore --> StoreOK{"Cửa hàng tồn tại?"}
    StoreOK -- Không --> ErrStore["❌ 404 STORE_NOT_FOUND<br>test: should_throw_when_store_not_found"]
    StoreOK -- Có --> ReadStore["Đọc cờ acceptsDinein, acceptsPickup, acceptsDelivery và prepTime"]
    ReadStore --> GenSlots["Gọi DateTimeUtils.generateTimeSlots sinh slot 30p theo giờ mở cửa thật"]
    
    GenSlots --> Success(["✅ 200 OK - FulfillmentOptionsResponse động<br>test: should_return_store_specific_options"])
    DefaultTenant --> SuccessDefault(["✅ 200 OK - FulfillmentOptionsResponse mặc định<br>test: should_return_default_options_when_storeId_null"])

    classDef ok fill:#dcfce7,stroke:#16a34a,stroke-width:2px;
    classDef fail fill:#fee2e2,stroke:#dc2626,stroke-width:2px;
    class Success,SuccessDefault ok;
    class ErrHdr,ErrStore fail;
```

### Biểu đồ Trình Tự (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as StoreController
    participant UC as GetFulfillmentOptionsUseCase
    participant SRepo as StoreRepository
    participant Time as DateTimeUtils

    C->>Ctrl: GET /api/v1/stores/fulfillment-options?storeId=... (X-Tenant-ID)
    Ctrl->>UC: execute(storeId)
    UC->>UC: TenantContext.getTenantId()
    alt Tenant ID == null
        UC-->>Ctrl: throw BusinessException(TENANT_HEADER_MISSING)
        Ctrl-->>C: 400 Bad Request
    end
    alt storeId == null
        UC->>Time: generateTimeSlots(null)
        Time-->>UC: defaultSlots
        UC-->>Ctrl: FulfillmentOptionsResponse (cấu hình mặc định toàn chuỗi)
        Ctrl-->>C: 200 OK
    else storeId != null
        UC->>SRepo: findByIdAndTenantId(storeId, tenantId)
        alt Store không tồn tại
            UC-->>Ctrl: throw BusinessException(STORE_NOT_FOUND)
            Ctrl-->>C: 404 Not Found
        end
        UC->>Time: generateTimeSlots(store.openingHours)
        Time-->>UC: dynamicSlots
        UC-->>Ctrl: FulfillmentOptionsResponse (theo cờ & giờ mở cửa thực của store)
        Ctrl-->>C: 200 OK
    end
```

---

## Use Case 7: Lưu & Bỏ Chi nhánh Yêu thích (ToggleFavoriteStoreUseCase)

- **API:** `POST & DELETE /api/v1/stores/{storeId}/favorite`
- **Use Case:** `ToggleFavoriteStoreUseCase`
- **Util:** `SecurityUtils.resolveCurrentUserId`
- **Test File:** `ToggleFavoriteStoreUseCaseTest.java`, `StoreControllerTest.java`

### Biểu đồ Luồng Quyết Định (Decision Tree & Test Cases)
```mermaid
flowchart TD
    Start(["Client gửi POST hoặc DELETE /api/v1/stores/{id}/favorite"]) --> CheckAuth{"Người dùng đã đăng nhập?<br>SecurityUtils.resolveCurrentUserId"}
    CheckAuth -- Chưa --> ErrAuth["❌ 401 UNAUTHORIZED<br>test: should_return_401_when_user_not_logged_in"]
    CheckAuth -- Đã login --> CheckTenant{"X-Tenant-ID hợp lệ?"}
    CheckTenant -- Không --> ErrHdr["❌ 400 TENANT_HEADER_MISSING<br>test: should_throw_when_tenant_is_null"]
    CheckTenant -- Có --> CheckStore["Kiểm tra Store tồn tại trong Tenant"]
    CheckStore --> StoreOK{"Store tồn tại?"}
    StoreOK -- Không --> ErrStore["❌ 404 STORE_NOT_FOUND<br>test: should_throw_when_store_does_not_exist"]
    
    StoreOK -- Có --> Action{"Hành động là POST hay DELETE?"}
    Action -- POST --> CheckAlready{"Đã có trong danh sách yêu thích?"}
    CheckAlready -- Chưa --> SaveDB["Tạo mới CustomerFavoriteStoreJpaEntity và lưu DB"]
    CheckAlready -- Đã có --> Skip["Bỏ qua không tạo trùng - Idempotent"]
    SaveDB --> RetPost(["✅ 200 OK - isFavorite: true<br>test: should_save_favorite_when_not_favorited_yet"])
    Skip --> RetPost
    
    Action -- DELETE --> DeleteDB["Xóa bản ghi khỏi customer_favorite_stores"]
    DeleteDB --> RetDel(["✅ 200 OK - isFavorite: false<br>test: should_delete_favorite_successfully"])

    classDef ok fill:#dcfce7,stroke:#16a34a,stroke-width:2px;
    classDef fail fill:#fee2e2,stroke:#dc2626,stroke-width:2px;
    class RetPost,RetDel ok;
    class ErrAuth,ErrHdr,ErrStore fail;
```

### Biểu đồ Trình Tự (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as StoreController
    participant Sec as SecurityUtils
    participant UC as ToggleFavoriteStoreUseCase
    participant SRepo as StoreRepository
    participant FRepo as CustomerFavoriteStoreRepository

    C->>Ctrl: POST / DELETE /api/v1/stores/{storeId}/favorite (X-User-ID)
    Ctrl->>Sec: resolveCurrentUserId(userIdHeader, principal)
    alt userId == null
        Ctrl-->>C: 401 Unauthorized
    end
    Ctrl->>UC: execute(storeId, userId, isFavorite)
    UC->>UC: TenantContext.getTenantId()
    alt Tenant ID == null
        UC-->>Ctrl: throw BusinessException(TENANT_HEADER_MISSING)
        Ctrl-->>C: 400 Bad Request
    end
    UC->>SRepo: existsByIdAndTenantId(storeId, tenantId)
    alt Store không tồn tại
        UC-->>Ctrl: throw BusinessException(STORE_NOT_FOUND)
        Ctrl-->>C: 404 Not Found
    end
    alt isFavorite == true (POST)
        UC->>FRepo: existsByTenantIdAndUserIdAndStoreId(...)
        alt Chưa có trong danh sách yêu thích
            UC->>FRepo: save(CustomerFavoriteStoreJpaEntity)
        end
        UC-->>Ctrl: FavoriteStoreToggleResponse(isFavorite=true)
    else isFavorite == false (DELETE)
        UC->>FRepo: deleteByTenantIdAndUserIdAndStoreId(...)
        UC-->>Ctrl: FavoriteStoreToggleResponse(isFavorite=false)
    end
    Ctrl-->>C: 200 OK
```

---

## Use Case 8: Danh sách Chi nhánh Yêu thích của Hội viên (GetFavoriteStoresUseCase)

- **API:** `GET /api/v1/stores/favorites?latitude=...&longitude=...`
- **Use Case:** `GetFavoriteStoresUseCase`
- **Util:** `SecurityUtils`, `GeoUtils`
- **Test File:** `GetFavoriteStoresUseCaseTest.java`, `StoreControllerTest.java`

### Biểu đồ Luồng Quyết Định (Decision Tree & Test Cases)
```mermaid
flowchart TD
    Start(["Client gọi GET /api/v1/stores/favorites"]) --> CheckAuth{"Người dùng đã đăng nhập?"}
    CheckAuth -- Chưa --> ErrAuth["❌ 401 UNAUTHORIZED<br>test: should_return_401_when_user_not_logged_in"]
    CheckAuth -- Có --> CheckTenant{"X-Tenant-ID hợp lệ?"}
    CheckTenant -- Không --> ErrHdr["❌ 400 TENANT_HEADER_MISSING<br>test: should_throw_when_tenant_is_null"]
    CheckTenant -- Có --> QueryFavs["Truy vấn customer_favorite_stores theo userId"]
    
    QueryFavs --> HasFavs{"Có chi nhánh nào không?"}
    HasFavs -- Không --> RetEmpty(["✅ 200 OK - Danh sách rỗng []<br>test: should_return_empty_list_when_user_has_no_favorites"])
    HasFavs -- Có --> LoadStores["Tải các chi nhánh đang Active thuộc Tenant"]
    LoadStores --> CalcDist["GeoUtils tính khoảng cách GPS nếu có tọa độ"]
    CalcDist --> SortDist["Sắp xếp khoảng cách tăng dần"]
    SortDist --> RetList(["✅ 200 OK - Danh sách chi nhánh với isFavorite=true<br>test: should_return_favorites_sorted_by_distance_when_gps_provided"])

    classDef ok fill:#dcfce7,stroke:#16a34a,stroke-width:2px;
    classDef fail fill:#fee2e2,stroke:#dc2626,stroke-width:2px;
    class RetEmpty,RetList ok;
    class ErrAuth,ErrHdr fail;
```

### Biểu đồ Trình Tự (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as StoreController
    participant Sec as SecurityUtils
    participant UC as GetFavoriteStoresUseCase
    participant FRepo as CustomerFavoriteStoreRepository
    participant SRepo as StoreRepository
    participant Geo as GeoUtils
    participant Map as TenantDtoMapper

    C->>Ctrl: GET /api/v1/stores/favorites?latitude=...&longitude=... (X-User-ID)
    Ctrl->>Sec: resolveCurrentUserId(userIdHeader, principal)
    alt userId == null
        Ctrl-->>C: 401 Unauthorized
    end
    Ctrl->>UC: execute(userId, lat, lng)
    UC->>UC: TenantContext.getTenantId()
    alt Tenant ID == null
        UC-->>Ctrl: throw BusinessException(TENANT_HEADER_MISSING)
        Ctrl-->>C: 400 Bad Request
    end
    UC->>FRepo: findByTenantIdAndUserId(tenantId, userId)
    FRepo-->>UC: List<CustomerFavoriteStoreJpaEntity>
    alt Không có chi nhánh yêu thích nào
        UC-->>Ctrl: emptyList()
        Ctrl-->>C: 200 OK (empty list)
    end
    UC->>SRepo: findAllById(storeIds)
    SRepo-->>UC: List<StoreJpaEntity>
    loop Mỗi Store
        opt Tọa độ GPS hợp lệ
            UC->>Geo: calculateDistanceKm(lat1, lon1, lat2, lon2)
            Geo-->>UC: distanceKm
        end
        UC->>Map: toStoreSummaryResponse(store, distanceKm, isFav=true)
    end
    UC->>UC: sortByDistanceIfAvailable()
    UC-->>Ctrl: List<StoreSummaryResponse>
    Ctrl-->>C: 200 OK
```

---

## 🎯 Tóm Tắt Ma Trận Kiểm Thử (65 Tests Checklist)

| Nhóm Kiểm Thử | File Test | Số Ca Kiểm Thử | Mục Tiêu Bao Phủ |
| :--- | :--- | :---: | :--- |
| **Tiện Ích Chung** | `GeoUtilsTest` | 5 | Công thức Haversine, làm tròn `BigDecimal`, kiểm tra biên tọa độ GPS. |
| | `QrCodeUtilsTest` | 4 | Regex bóc tách số bàn, format số bàn, normalize token. |
| | `SecurityUtilsTest` | 5 | Parse UUID an toàn, trích xuất User ID từ Header hoặc Principal. |
| | `DateTimeUtilsTest` | 2 | Sinh slot 30 phút theo giờ mở cửa thật, fallback slot mặc định. |
| **Application Layer** | `GetTenantInfoUseCaseTest` | 3 | Kiểm tra tenant hợp lệ, missing header, không tìm thấy tenant. |
| | `GetStoresListUseCaseTest` | 4 | Tìm kiếm, lọc fulfillment, tính khoảng cách GPS, cờ yêu thích. |
| | `GetStoreDetailUseCaseTest` | 3 | Trả chi tiết quán, missing header, store không tồn tại. |
| | `GetStoreTablesUseCaseTest` | 3 | Lấy danh sách bàn theo quán, missing header, store không tồn tại. |
| | `ResolveTableQrUseCaseTest` | 5 | Giải mã 3 tầng: Exact, Normalized, Fallback Regex, bắt token lỗi. |
| | `GetFulfillmentOptionsUseCaseTest` | 4 | Tùy chọn động theo quán, tùy chọn mặc định, missing header, store 404. |
| | `ToggleFavoriteStoreUseCaseTest` | 4 | Thêm mới, thêm trùng (idempotent), xóa yêu thích, store không tồn tại. |
| | `GetFavoriteStoresUseCaseTest` | 3 | Danh sách rỗng, danh sách có sắp xếp khoảng cách GPS, missing header. |
| **Presentation Layer** | `TenantControllerTest` | 3 | Kiểm thử WebMvc MockMvc với các mã HTTP 200, 400. |
| | `StoreControllerTest` | 12 | Kiểm thử WebMvc MockMvc đầy đủ cho cả 6 endpoint Stores & Security. |
| **Core Modules** | `PointsLedgerJpaEntityTest` | 2 | Kiểm thử bất biến số dư điểm thưởng Ledger. |
| | `MemberTierTest` | 2 | Kiểm thử 6 hạng hội viên Loyalty. |
| | `OrderJpaEntityTest` | 1 | Kiểm thử Entity đơn hàng và tính thuế. |
| **Tổng Cộng** | **17 Test Suites** | **65 Tests** | **🟢 ĐẠT 100% PASS** |
