# Business Flows & Architecture Sequences: Catalog & Menu Engine

Tài liệu thiết kế luồng nghiệp vụ (Business Flowcharts) và biểu đồ tương tác 4 lớp Clean Architecture (Sequence Diagrams) cho Phân hệ Thực Đơn, Món Ăn, Nhóm Tùy Chọn Modifier & Banner Khuyến Mãi (Catalog Module).

---

## 1. Tổng quan Luồng Nghiệp vụ Catalog (End-to-End Business Flowchart)

Biểu đồ Flowchart tổng thể mô tả hành trình khám phá thực đơn, lọc danh mục, duyệt cây phân cấp, xem chi tiết món ăn kèm tùy chọn modifier và hiển thị banner chiến dịch tiếp thị.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#94a3b8', 'secondaryColor': '#0f172a', 'tertiaryColor': '#1e293b'}}}%%
flowchart TD
    Client(["Khách hàng / Kiosk POS"])
    
    subgraph DiscoveryFlow["Luồng Khám Phá Thực Đơn & Banner"]
        Banners["1. Lấy Banner Tiếp Thị Kích Hoạt<br>(Hot Deals, Seasonal)"]
        Categories["2. Lấy Danh Mục Chính & Subcategory Pills<br>(Popular, Maki, Nigiri, Drinks...)"]
        MenuTree["3. Tải Toàn Bộ Cây Thực Đơn Phân Cấp<br>(Section List: Cat ➜ Subcat ➜ Products)"]
    end
    
    subgraph SelectionFlow["Luồng Tùy Biến Món Ăn"]
        ProductClick{"Khách hàng chọn món"}
        ProductDetail["4. Chi Tiết Món & Nhóm Tùy Chọn<br>(Original Price, Pieces, Dietary Tags)"]
        ModifierSelection["5. Modal Tùy Biến Modifiers<br>(Size: M/L, Toppings, Đường, Đá)"]
        ValidationCheck{"Thỏa mãn ràng buộc<br>min/max selection?"}
        AddToCart["Thêm vào Giỏ Hàng (CartContext)"]
    end

    Client --> Banners
    Client --> Categories
    Client --> MenuTree
    MenuTree --> ProductClick
    ProductClick -->|Chạm vào Card món| ProductDetail
    ProductDetail --> ModifierSelection
    ModifierSelection --> ValidationCheck
    ValidationCheck -->|Hợp lệ| AddToCart
    ValidationCheck -->|Chưa đủ min hoặc vượt max| ModifierSelection
```

---

## 2. Sequence Diagram Clean Architecture: 6 Use Cases

### UC-2.1: Lấy Danh Mục Chính Kèm Subcategories
**Endpoint:** `GET /api/v1/stores/{storeId}/categories`  
**Mô tả:** Trả về danh sách danh mục phân loại thực đơn chính kèm theo các nút pill nhóm con phục vụ thanh cuộn ngang.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Client App (PWA/Kiosk)
    participant C as CategoryController<br>(Presentation)
    participant UC as GetCategoriesUseCaseImpl<br>(Application Orchestrator)
    participant SR as StoreRepository<br>(Infrastructure)
    participant CR as CategoryRepository<br>(Infrastructure)
    participant SCR as SubcategoryRepository<br>(Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>C: GET /api/v1/stores/{storeId}/categories<br>[Header: X-Tenant-ID]
    activate C
    C->>UC: execute(storeId)
    activate UC
    
    Note over UC: Step 1: Lấy tenantId từ TenantContext
    UC->>SR: findByIdAndTenantId(storeId, tenantId)
    activate SR
    SR->>DB: SELECT store WHERE id = storeId AND tenant_id = tenantId
    DB-->>SR: StoreJpaEntity
    SR-->>UC: Optional<StoreJpaEntity>
    deactivate SR

    Note over UC: Step 2: Truy vấn Categories của Tenant
    UC->>CR: findByTenantIdAndActiveOrderByDisplayOrderAsc(tenantId, true)
    activate CR
    CR->>DB: SELECT categories WHERE tenant_id = tenantId AND is_active = true
    DB-->>CR: List<CategoryJpaEntity>
    CR-->>UC: List<CategoryJpaEntity>
    deactivate CR

    Note over UC: Step 3: Batch query Subcategories
    UC->>SCR: findByCategoryIdInAndActiveOrderByDisplayOrderAsc(categoryIds, true)
    activate SCR
    SCR->>DB: SELECT subcategories WHERE category_id IN (...)
    DB-->>SCR: List<SubcategoryJpaEntity>
    SCR-->>UC: List<SubcategoryJpaEntity>
    deactivate SCR

    Note over UC: Step 4: Ghép nhóm subcategories vào categories
    UC-->>C: List<CategoryResponse>
    deactivate UC
    C-->>Client: 200 OK: ApiResponse<List<CategoryResponse>>
    deactivate C
```

---

### UC-2.2: Lấy Danh Mục Con Theo Danh Mục Chính
**Endpoint:** `GET /api/v1/categories/{categoryId}/subcategories`  
**Mô tả:** Trả về danh sách nhóm con (Subcategory pills) của một danh mục chỉ định.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Client App (PWA/Kiosk)
    participant C as CategoryController<br>(Presentation)
    participant UC as GetSubcategoriesUseCaseImpl<br>(Application Orchestrator)
    participant CR as CategoryRepository<br>(Infrastructure)
    participant SCR as SubcategoryRepository<br>(Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>C: GET /api/v1/categories/{categoryId}/subcategories<br>[Header: X-Tenant-ID]
    activate C
    C->>UC: execute(categoryId)
    activate UC

    Note over UC: Step 1: Xác thực Category thuộc Tenant
    UC->>CR: findByIdAndTenantId(categoryId, tenantId)
    activate CR
    CR->>DB: SELECT category WHERE id = categoryId AND tenant_id = tenantId
    DB-->>CR: Optional<CategoryJpaEntity>
    CR-->>UC: Optional<CategoryJpaEntity>
    deactivate CR

    Note over UC: Step 2: Lấy Subcategories theo display_order
    UC->>SCR: findByCategoryIdAndActiveOrderByDisplayOrderAsc(categoryId, true)
    activate SCR
    SCR->>DB: SELECT subcategories WHERE category_id = categoryId
    DB-->>SCR: List<SubcategoryJpaEntity>
    SCR-->>UC: List<SubcategoryJpaEntity>
    deactivate SCR

    Note over UC: Step 3: Ánh xạ sang SubcategoryResponse
    UC-->>C: List<SubcategoryResponse>
    deactivate UC
    C-->>Client: 200 OK: ApiResponse<List<SubcategoryResponse>>
    deactivate C
```

---

### UC-2.3: Lấy Cây Thực Đơn Đầy Đủ (Get Store Menu)
**Endpoint:** `GET /api/v1/stores/{storeId}/menu`  
**Mô tả:** Trả về cây thực đơn phân cấp hoàn chỉnh (Categories ➜ Subcategories ➜ Products) dạng Section List cho màn hình Menu.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Client App (PWA/Kiosk)
    participant PC as ProductController<br>(Presentation)
    participant UC as GetStoreMenuUseCaseImpl<br>(Application Orchestrator)
    participant SR as StoreRepository<br>(Infrastructure)
    participant CR as CategoryRepository<br>(Infrastructure)
    participant SCR as SubcategoryRepository<br>(Infrastructure)
    participant PR as ProductRepository<br>(Infrastructure)
    participant MGR as ProductModifierGroupRepository<br>(Infrastructure)
    participant MAP as CatalogDtoMapper<br>(Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>PC: GET /api/v1/stores/{storeId}/menu<br>[Header: X-Tenant-ID]
    activate PC
    PC->>UC: execute(storeId)
    activate UC

    Note over UC: Step 1: Xác thực Store thuộc Tenant
    UC->>SR: findByIdAndTenantId(storeId, tenantId)
    activate SR
    SR-->>UC: Optional<StoreJpaEntity>
    deactivate SR

    Note over UC: Step 2: Batch fetch Categories, Subcategories, Products
    UC->>CR: findByTenantIdAndActiveOrderByDisplayOrderAsc(tenantId, true)
    activate CR
    CR-->>UC: List<CategoryJpaEntity>
    deactivate CR

    UC->>SCR: findByCategoryIdInAndActiveOrderByDisplayOrderAsc(categoryIds, true)
    activate SCR
    SCR-->>UC: List<SubcategoryJpaEntity>
    deactivate SCR

    UC->>PR: findByTenantIdAndAvailableOrderByCreatedAtDesc(tenantId, true)
    activate PR
    PR-->>UC: List<ProductJpaEntity>
    deactivate PR

    Note over UC: Step 3: Xác định cờ hasModifiers theo batch
    UC->>MGR: findByProductIdInOrderByDisplayOrderAsc(productIds)
    activate MGR
    MGR-->>UC: List<ProductModifierGroupJpaEntity>
    deactivate MGR

    Note over UC: Step 4: Xử lý dietaryTags & Lắp ráp cây phân cấp
    UC->>MAP: parseDietaryTags(json)
    activate MAP
    MAP-->>UC: List<String>
    deactivate MAP

    Note over UC: Step 5: Đóng gói StoreMenuResponse
    UC-->>PC: StoreMenuResponse
    deactivate UC
    PC-->>Client: 200 OK: ApiResponse<StoreMenuResponse>
    deactivate PC
```

---

### UC-2.4: Lấy Chi Tiết Món Ăn (Get Product Detail)
**Endpoint:** `GET /api/v1/products/{productId}`  
**Mô tả:** Lấy thông tin chi tiết một món ăn kèm originalPrice, piecesText, dietaryTags và toàn bộ các nhóm modifier (bắt buộc / tùy chọn).

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Client App (PWA/Kiosk)
    participant PC as ProductController<br>(Presentation)
    participant UC as GetProductDetailUseCaseImpl<br>(Application Orchestrator)
    participant PR as ProductRepository<br>(Infrastructure)
    participant MGR as ProductModifierGroupRepository<br>(Infrastructure)
    participant MR as ProductModifierRepository<br>(Infrastructure)
    participant MAP as CatalogDtoMapper<br>(Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>PC: GET /api/v1/products/{productId}<br>[Header: X-Tenant-ID]
    activate PC
    PC->>UC: execute(productId)
    activate UC

    Note over UC: Step 1: Xác thực Product thuộc Tenant
    UC->>PR: findByIdAndTenantId(productId, tenantId)
    activate PR
    PR->>DB: SELECT product WHERE id = productId AND tenant_id = tenantId
    DB-->>PR: ProductJpaEntity
    PR-->>UC: Optional<ProductJpaEntity>
    deactivate PR

    Note over UC: Step 2: Lấy danh sách nhóm Modifier
    UC->>MGR: findByProductIdOrderByDisplayOrderAsc(productId)
    activate MGR
    MGR-->>UC: List<ProductModifierGroupJpaEntity>
    deactivate MGR

    Note over UC: Step 3: Batch query options chi tiết trong nhóm
    UC->>MR: findByGroupIdIn(groupIds)
    activate MR
    MR-->>UC: List<ProductModifierJpaEntity>
    deactivate MR

    Note over UC: Step 4: Parse dietaryTags & Đóng gói DTO
    UC->>MAP: parseDietaryTags(json)
    activate MAP
    MAP-->>UC: List<String>
    deactivate MAP

    UC-->>PC: ProductDetailResponse
    deactivate UC
    PC-->>Client: 200 OK: ApiResponse<ProductDetailResponse>
    deactivate PC
```

---

### UC-2.5: Lấy Nhóm Tùy Chọn Modifiers Của Món
**Endpoint:** `GET /api/v1/products/{productId}/modifiers`  
**Mô tả:** Lấy riêng danh sách các nhóm tùy chọn (Size, Topping, Mức ngọt, Lượng đá) phục vụ Modal tùy biến trên Frontend.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Client App (PWA/Kiosk)
    participant PC as ProductController<br>(Presentation)
    participant UC as GetProductModifiersUseCaseImpl<br>(Application Orchestrator)
    participant PR as ProductRepository<br>(Infrastructure)
    participant MGR as ProductModifierGroupRepository<br>(Infrastructure)
    participant MR as ProductModifierRepository<br>(Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>PC: GET /api/v1/products/{productId}/modifiers<br>[Header: X-Tenant-ID]
    activate PC
    PC->>UC: execute(productId)
    activate UC

    Note over UC: Step 1: Kiểm tra Product tồn tại
    UC->>PR: findByIdAndTenantId(productId, tenantId)
    activate PR
    PR-->>UC: Optional<ProductJpaEntity>
    deactivate PR

    Note over UC: Step 2: Lấy các nhóm Modifier (Size, Topping...)
    UC->>MGR: findByProductIdOrderByDisplayOrderAsc(productId)
    activate MGR
    MGR-->>UC: List<ProductModifierGroupJpaEntity>
    deactivate MGR

    Note over UC: Step 3: Lấy các option chi tiết theo Group IDs
    UC->>MR: findByGroupIdIn(groupIds)
    activate MR
    MR-->>UC: List<ProductModifierJpaEntity>
    deactivate MR

    Note over UC: Step 4: Lắp ráp DTO ModifierGroupResponse
    UC-->>PC: List<ModifierGroupResponse>
    deactivate UC
    PC-->>Client: 200 OK: ApiResponse<List<ModifierGroupResponse>>
    deactivate PC
```

---

### UC-2.6: Lấy Banner Khuyến Mãi Tiếp Thị
**Endpoint:** `GET /api/v1/stores/{storeId}/promotions/banners`  
**Mô tả:** Lấy danh sách banner chiến dịch tiếp thị đang kích hoạt cho chi nhánh hoặc áp dụng toàn chuỗi.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Client App (PWA/Kiosk)
    participant C as PromotionController<br>(Presentation)
    participant UC as GetPromotionBannersUseCaseImpl<br>(Application Orchestrator)
    participant SR as StoreRepository<br>(Infrastructure)
    participant BR as PromotionBannerRepository<br>(Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>C: GET /api/v1/stores/{storeId}/promotions/banners<br>[Header: X-Tenant-ID]
    activate C
    C->>UC: execute(storeId)
    activate UC

    Note over UC: Step 1: Xác thực Store thuộc Tenant
    UC->>SR: findByIdAndTenantId(storeId, tenantId)
    activate SR
    SR-->>UC: Optional<StoreJpaEntity>
    deactivate SR

    Note over UC: Step 2: Lấy Banner (store_id = storeId OR store_id IS NULL)
    UC->>BR: findActiveBanners(tenantId, storeId)
    activate BR
    BR->>DB: SELECT promotion_banners WHERE tenant_id = tenantId AND is_active = true AND (store_id = storeId OR store_id IS NULL) ORDER BY display_order ASC
    DB-->>BR: List<PromotionBannerJpaEntity>
    BR-->>UC: List<PromotionBannerJpaEntity>
    deactivate BR

    Note over UC: Step 3: Ánh xạ sang PromotionBannerResponse
    UC-->>C: List<PromotionBannerResponse>
    deactivate UC
    C-->>Client: 200 OK: ApiResponse<List<PromotionBannerResponse>>
    deactivate C
```
