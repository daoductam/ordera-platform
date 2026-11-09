# Business Flows & Architecture Sequences: Order, Checkout & E-Receipt Module

Tài liệu thiết kế luồng nghiệp vụ tổng thể (Business Flowcharts) và biểu đồ tương tác 4 lớp Clean Architecture (Sequence Diagrams) cho Phân Hệ Giỏ Hàng, Đặt Món, Tính Thuế VAT 15%, Tips, Checkout & Hóa Đơn E-Receipt (Order Module).

---

## 1. Tổng quan Luồng Nghiệp vụ Order & Checkout (End-to-End Business Flowchart)

Biểu đồ Flowchart tổng thể mô tả hành trình từ lúc khách hàng duyệt giỏ hàng, tùy chỉnh số lượng / ghi chú dị ứng, dự toán bảng tính chi phí (VAT 15%, voucher, tip), chốt đơn thanh toán an toàn qua Redisson Distributed Lock và Idempotency, theo dõi đơn tại bếp, đến xuất hóa đơn điện tử E-Receipt PDF.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#94a3b8', 'secondaryColor': '#0f172a', 'tertiaryColor': '#1e293b'}}}%%
flowchart TD
    Client(["Khách hàng / Mobile PWA / Kiosk"])

    subgraph CartFlow["1. Quản lý Giỏ Hàng (Hybrid Cache: Redis 7-day TTL + Postgres)"]
        ViewCart["6.1 Xem Giỏ Hàng<br>(Get My Cart)"]
        AddToCart["6.2 Thêm Món & Topping<br>(Add Item to Cart)"]
        UpdateCart["6.3 Sửa Số Lượng / Xóa Món<br>(Update / Delete Item)"]
        AddComment["6.4 Ghi Chú Dị Ứng<br>(Max 200 ký tự)"]
    end

    subgraph CheckoutCalculation["2. Dự Toán & Động Cơ Tính Toán Tài Chính"]
        PreviewReq["6.5 Yêu Cầu Dự Toán Checkout<br>(Voucher, Quà Tặng, Tiền Tip)"]
        CalcEngine["OrderCalculationUtils<br>• Subtotal Amount<br>• Voucher Discount (Kiểm tra Min Order)<br>• Voluntary Tip (Null-safe)<br>• VAT 15% Included: Total * 15 / 115<br>• Points to Earn: floor((Subtotal - Discount) * Rate)"]
        PreviewResp["Bảng Dự Toán Minh Bạch<br>(NOK Currency, Chi Tiết Khấu Trừ)"]
    end

    subgraph SecureCheckout["3. Chốt Đơn & Thanh Toán An Toàn (ACID Transaction)"]
        PayClick{"Bấm Thanh Toán (Pay)"}
        IdemCheck{"Kiểm Tra Idempotency-Key<br>(Redis TTL 24h)"}
        RedissonLock{"Chiếm Giữ Redisson Lock<br>lock:order:checkout:{userId}"}
        CreateOrder["Tạo Đơn Hàng Mới<br>• Sinh Số Thứ Tự: #005<br>• Mã Tra Cứu: ORD-YYYYMMDD-XXXX<br>• Lưu Items & Modifiers"]
        MarkVoucher["Đổi Trạng Thái Voucher ➜ USED"]
        AccruePoints["AccruePointsUseCase<br>Ghi Sổ Cái Bất Biến points_ledger (ORDER_EARN)"]
        ClearCart["Xóa Giỏ Hàng Khỏi Redis & Postgres"]
        ReleaseLock["Giải Phóng Redisson Lock"]
    end

    subgraph PostOrder["4. Hậu Mãi, Tiến Trình Chế Biến & E-Receipt"]
        ActiveTab["6.7 Danh Sách Đơn Hàng<br>Tab ACTIVE vs Tab PAST"]
        KitchenTimeline["6.8 Chi Tiết Đơn & Timeline Bếp<br>PLACED ➜ IN_KITCHEN ➜ READY"]
        Reorder["6.9 Đặt Lại Đơn Cũ 1-Click<br>(Clone Items sang Giỏ Mới)"]
        EmailReceipt["6.10 Xuất E-Receipt PDF & Gửi Email<br>(JavaMailSender + PDF 1.4 Byte Stream)"]
    end

    Client --> ViewCart
    Client --> AddToCart
    AddToCart --> ViewCart
    ViewCart --> UpdateCart
    ViewCart --> AddComment
    ViewCart --> PreviewReq
    PreviewReq --> CalcEngine
    CalcEngine --> PreviewResp
    PreviewResp --> PayClick
    PayClick --> IdemCheck
    IdemCheck -->|Đã tồn tại| RejectIdem["409 Conflict: Giao dịch trùng lặp"]
    IdemCheck -->|Hợp lệ| RedissonLock
    RedissonLock --> CreateOrder
    CreateOrder --> MarkVoucher
    MarkVoucher --> AccruePoints
    AccruePoints --> ClearCart
    ClearCart --> ReleaseLock
    ReleaseLock --> ActiveTab
    ActiveTab --> KitchenTimeline
    ActiveTab --> Reorder
    KitchenTimeline --> EmailReceipt
```

---

## 2. Sequence Diagram Clean Architecture: 10 Use Cases

### UC-6.1: Lấy Giỏ Hàng Hiện Tại (Get My Cart)
**Endpoint:** `GET /api/v1/cart`  
**Mô tả:** Lấy danh sách món ăn, số lượng, tùy chọn modifier, ghi chú dị ứng và tổng tiền tạm tính từ Redis Cache (với database fallback).

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng (PWA/Kiosk)
    participant C as CartController<br>(Presentation)
    participant CS as CartService<br>(Application)
    participant RC as RedisCartStorageAdapter<br>(Infrastructure)
    participant CR as CartJpaRepository<br>(Infrastructure)
    participant DB as PostgreSQL 16

    Client->>C: GET /api/v1/cart [X-Tenant-ID, X-User-ID]
    activate C
    C->>CS: getMyCart(tenantId, userId, storeId)
    activate CS
    CS->>RC: getCartByUserId(tenantId, userId)
    activate RC
    alt Cache Hit trên Redis
        RC-->>CS: Cart (Domain Object)
    else Cache Miss
        RC-->>CS: Optional.empty()
        deactivate RC
        CS->>CR: findByTenantIdAndUserId(tenantId, userId)
        activate CR
        CR->>DB: SELECT * FROM carts WHERE tenant_id = ? AND user_id = ?
        DB-->>CR: CartJpaEntity
        CR-->>CS: Optional<CartJpaEntity>
        deactivate CR
        CS->>RC: saveCart(Cart) [TTL 7 Days]
    end
    CS-->>C: CartResponse
    deactivate CS
    C-->>Client: 200 OK: ApiResponse.ok(MSG_CART_RETRIEVED, CartResponse)
    deactivate C
```

---

### UC-6.2: Thêm Món Vào Giỏ Hàng Kèm Modifier (Add Item to Cart)
**Endpoint:** `POST /api/v1/cart/items`  
**Mô tả:** Thêm món ăn và tùy chọn modifier vào giỏ. Nếu cùng bộ tùy chọn thì cộng dồn số lượng.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng (PWA/Kiosk)
    participant C as CartController<br>(Presentation)
    participant CS as CartService<br>(Application)
    participant PR as ProductRepository<br>(Infrastructure)
    participant PMR as ProductModifierRepository<br>(Infrastructure)
    participant U as OrderCalculationUtils<br>(Domain Math)
    participant RC as RedisCartStorageAdapter<br>(Infrastructure)
    participant DB as PostgreSQL 16

    Client->>C: POST /api/v1/cart/items [AddCartItemRequest]
    activate C
    C->>CS: addItem(tenantId, userId, request)
    activate CS
    CS->>PR: findByIdAndTenantId(productId, tenantId)
    activate PR
    PR->>DB: SELECT * FROM products WHERE id = ? AND is_available = true
    DB-->>PR: ProductJpaEntity
    PR-->>CS: Optional<ProductJpaEntity>
    deactivate PR

    CS->>PMR: findAllById(selectedModifierIds)
    activate PMR
    PMR-->>CS: List<ProductModifierJpaEntity>
    deactivate PMR

    Note over CS,U: Tính thành tiền itemTotal = (basePrice + sum(extraPrice)) * qty
    CS->>U: calculateItemTotal(basePrice, modifierPrices, quantity)
    U-->>CS: itemTotal (scale = 2)

    Note over CS: Gộp món nếu trùng cấu hình hoặc thêm CartItem mới
    CS->>U: calculateSubtotal(items)
    U-->>CS: cartSubtotal

    CS->>RC: saveCart(cart) [Gia hạn TTL 7 Days]
    CS-->>C: CartResponse
    deactivate CS
    C-->>Client: 201 Created: ApiResponse.ok(MSG_ITEM_ADDED_TO_CART, CartResponse)
    deactivate C
```

---

### UC-6.3: Cập Nhật Số Lượng / Xóa Món Khỏi Giỏ (Update / Delete Cart Item)
**Endpoint:** `PATCH /api/v1/cart/items/{itemId}` hoặc `DELETE /api/v1/cart/items/{itemId}`  
**Mô tả:** Tăng/giảm số lượng hoặc xóa bỏ dòng món ăn khỏi giỏ hàng.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng
    participant C as CartController<br>(Presentation)
    participant CS as CartService<br>(Application)
    participant U as OrderCalculationUtils<br>(Domain Math)
    participant RC as RedisCartStorageAdapter<br>(Infrastructure)

    Client->>C: PATCH /api/v1/cart/items/{itemId} { quantity: 3 }
    activate C
    C->>CS: updateItemQuantity(tenantId, userId, itemId, 3)
    activate CS
    Note over CS: Tìm item trong giỏ. Nếu qty = 0 ➜ xóa item
    CS->>U: calculateItemTotal(...) & calculateSubtotal(...)
    U-->>CS: New Subtotal
    CS->>RC: saveCart(cart)
    CS-->>C: CartResponse
    deactivate CS
    C-->>Client: 200 OK: ApiResponse.ok(MSG_CART_ITEM_UPDATED, CartResponse)
    deactivate C
```

---

### UC-6.4: Cập Nhật Ghi Chú Đơn Hàng / Dị Ứng (Update Cart Comment)
**Endpoint:** `PUT /api/v1/cart/comment`  
**Mô tả:** Cập nhật yêu cầu đặc biệt hoặc thông tin dị ứng với độ dài tối đa 200 ký tự.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng
    participant C as CartController<br>(Presentation)
    participant CS as CartService<br>(Application)
    participant RC as RedisCartStorageAdapter<br>(Infrastructure)
    participant CR as CartJpaRepository<br>(Infrastructure)

    Client->>C: PUT /api/v1/cart/comment { comment: "Dị ứng mè rang" }
    activate C
    C->>CS: updateComment(tenantId, userId, comment)
    activate CS
    Note over CS: Validate length <= MAX_ALLERGY_NOTE_LENGTH (200)
    CS->>RC: saveCart(cart)
    CS->>CR: save(cartJpaEntity)
    CS-->>C: CartResponse
    deactivate CS
    C-->>Client: 200 OK: ApiResponse.ok(MSG_ALLERGY_NOTE_UPDATED, CartResponse)
    deactivate C
```

---

### UC-6.5: Dự Toán Bảng Tính Checkout (Preview Checkout Calculation)
**Endpoint:** `POST /api/v1/orders/checkout/preview`  
**Mô tả:** Dự toán Subtotal, Discount, Tip, VAT 15% (đã bao gồm trong giá), Total và Điểm tích lũy dự kiến (+170 pts).

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng (CheckoutView)
    participant C as OrderController<br>(Presentation)
    participant UC as PreviewCheckoutCalculationUseCaseImpl<br>(Application)
    participant CS as CartService<br>(Application)
    participant VR as VoucherJpaRepository<br>(Infrastructure)
    participant U as OrderCalculationUtils<br>(Domain Math)

    Client->>C: POST /api/v1/orders/checkout/preview [PreviewCheckoutRequest]
    activate C
    C->>UC: execute(tenantId, userId, request)
    activate UC

    UC->>CS: getCartDomain(tenantId, request.getCartId())
    CS-->>UC: Cart (Subtotal: 250.00 NOK)

    alt Áp dụng Voucher Code
        UC->>VR: findByTenantIdAndCode(tenantId, voucherCode)
        VR-->>UC: VoucherJpaEntity (Min: 200 NOK, -30%)
        Note over UC: Kiểm tra Subtotal >= MinOrderValue<br>Tính discount = 75.00 NOK
    end

    Note over UC,U: Tính toán số học chính xác tuyệt đối (scale = 2)
    UC->>U: calculateTotal(subtotal: 250, discount: 75, tip: 10)
    U-->>UC: total = 185.00 NOK

    UC->>U: calculateVat(total: 185.00) ➜ Total * 15 / 115
    U-->>UC: vat = 24.13 NOK

    UC->>U: calculatePointsToEarn(subtotal: 250, discount: 75, tierRate: 1.0)
    Note over U: Tiền Tip KHÔNG được tính điểm thưởng
    U-->>UC: pointsToEarn = 175 pts

    UC-->>C: PreviewCheckoutResponse
    deactivate UC
    C-->>Client: 200 OK: ApiResponse.ok(MSG_CHECKOUT_PREVIEW_CALCULATED, PreviewCheckoutResponse)
    deactivate C
```

---

### UC-6.6: Khởi Tạo Đơn Hàng & Thanh Toán (Checkout Order Orchestrator)
**Endpoint:** `POST /api/v1/orders/checkout`  
**Mô tả:** Chốt đơn thanh toán an toàn qua Redisson Lock, kiểm tra Idempotency 24h, tạo đơn hàng (#005, ORD-20261105-0891), khóa Voucher sang USED, tự động ghi sổ cái points_ledger (ORDER_EARN), và dọn dẹp giỏ hàng.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng (Nút Pay)
    participant C as OrderController<br>(Presentation)
    participant UC as CheckoutOrderUseCaseImpl<br>(Orchestrator)
    participant RC as RedisCartStorageAdapter<br>(Infrastructure)
    participant Redisson as Redisson Lock Manager<br>(Infrastructure)
    participant OR as OrderJpaRepository<br>(Infrastructure)
    participant UVR as UserVoucherJpaRepository<br>(Infrastructure)
    participant Accrue as AccruePointsUseCase<br>(Loyalty Module)
    participant CS as CartService<br>(Application)
    participant Mail as JavaMailReceiptAdapter<br>(Infrastructure)

    Client->>C: POST /api/v1/orders/checkout [Header: Idempotency-Key]
    activate C
    C->>UC: execute(tenantId, userId, idempotencyKey, request)
    activate UC

    Note over UC: 1. checkIdempotency(tenantId, idempotencyKey)
    UC->>RC: getIdempotentOrderId(tenantId, idempotencyKey)
    RC-->>UC: Optional.empty() (Chưa từng xử lý)

    Note over UC: 2. acquireCheckoutLock(userId)
    UC->>Redisson: getLock("lock:order:checkout:" + userId)
    UC->>Redisson: tryLock(wait: 5s, lease: 10s)
    Redisson-->>UC: Acquired

    Note over UC: 3. validateAndCalculateAmounts()
    Note over UC: 4. createOrderEntities() (Mã #005, ORD-20261105-0891)
    UC->>OR: save(OrderJpaEntity & OrderItemJpaEntities)
    OR-->>UC: Persisted Order

    Note over UC: 5. applyRewardsAndPoints()
    UC->>UVR: save(userVoucher.setStatus("USED"))
    UC->>Accrue: execute(AccruePointsCommand: ORDER_EARN, +170 pts)
    Accrue-->>UC: PointsLedger Entry

    Note over UC: 6. clearCartCache()
    UC->>CS: clearCart(tenantId, cartId, userId)

    Note over UC: 7. dispatchReceiptEmail()
    opt Có eReceiptEmail
        UC->>Mail: sendReceiptEmail(order, items, email)
    end

    Note over UC: 8. recordIdempotency(TTL 24h)
    UC->>RC: saveIdempotencyRecord(tenantId, idempotencyKey, orderId)

    UC->>Redisson: unlock()
    UC-->>C: CheckoutOrderResponse
    deactivate UC
    C-->>Client: 201 Created: ApiResponse.ok(MSG_ORDER_CHECKOUT_SUCCESS, CheckoutOrderResponse)
    deactivate C
```

---

### UC-6.7: Danh Sách Đơn Hàng Của Tôi (List My Orders)
**Endpoint:** `GET /api/v1/orders`  
**Mô tả:** Lấy danh sách lịch sử đơn hàng phân chia theo 2 tab `ACTIVE` (đang xử lý) hoặc `PAST` (đã hoàn tất/hủy) có phân trang.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng (OrdersPage)
    participant C as OrderController<br>(Presentation)
    participant UC as GetMyOrdersUseCaseImpl<br>(Application)
    participant OR as OrderJpaRepository<br>(Infrastructure)
    participant OIR as OrderItemJpaRepository<br>(Infrastructure)

    Client->>C: GET /api/v1/orders?tab=ACTIVE&page=0&size=10
    activate C
    C->>UC: execute(tenantId, userId, "ACTIVE", pageable)
    activate UC

    Note over UC: Lọc status IN [PENDING, CONFIRMED, PREPARING, READY]
    UC->>OR: findByTenantIdAndUserIdAndStatusInOrderByCreatedAtDesc(...)
    OR-->>UC: Page<OrderJpaEntity>

    loop Cho từng Order trong Page
        UC->>OIR: findByOrderId(order.getId())
        OIR-->>UC: List<OrderItemJpaEntity>
    end

    UC-->>C: PageResponse<OrderSummaryResponse>
    deactivate UC
    C-->>Client: 200 OK: ApiResponse.ok(MSG_ORDERS_LIST_RETRIEVED, PageResponse)
    deactivate C
```

---

### UC-6.8: Chi Tiết Đơn Hàng & Tiến Trình Bếp (Get Order Details)
**Endpoint:** `GET /api/v1/orders/{orderId}`  
**Mô tả:** Trả về thông tin chi tiết đơn hàng, số thứ tự nhận món (#005), 3 bước tiến trình bếp (PLACED, IN_KITCHEN, READY_FOR_PICKUP) và tóm tắt tài chính.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng
    participant C as OrderController<br>(Presentation)
    participant UC as GetOrderDetailUseCaseImpl<br>(Application)
    participant OR as OrderJpaRepository<br>(Infrastructure)
    participant OIR as OrderItemJpaRepository<br>(Infrastructure)
    participant OMR as OrderItemModifierJpaRepository<br>(Infrastructure)
    participant SR as StoreRepository<br>(Infrastructure)

    Client->>C: GET /api/v1/orders/{orderId}
    activate C
    C->>UC: execute(tenantId, userId, orderId)
    activate UC

    UC->>OR: findByIdAndTenantId(orderId, tenantId)
    OR-->>UC: OrderJpaEntity (Kiểm tra quyền sở hữu IDOR)

    UC->>SR: findByIdAndTenantId(storeId, tenantId)
    SR-->>UC: StoreJpaEntity (Linn Sushi Sentrum)

    UC->>OIR: findByOrderId(orderId)
    OIR-->>UC: List<OrderItemJpaEntity>

    loop Cho từng món ăn
        UC->>OMR: findByOrderItemId(item.getId())
        OMR-->>UC: List<OrderItemModifierJpaEntity>
    end

    Note over UC: Xây dựng timeline 3 bước: PLACED, IN_KITCHEN, READY_FOR_PICKUP
    UC-->>C: OrderDetailResponse
    deactivate UC
    C-->>Client: 200 OK: ApiResponse.ok(MSG_ORDER_DETAIL_RETRIEVED, OrderDetailResponse)
    deactivate C
```

---

### UC-6.9: Đặt Lại Đơn Hàng Nhanh 1-Click (Reorder Past Order)
**Endpoint:** `POST /api/v1/orders/{orderId}/reorder`  
**Mô tả:** Sao chép các món ăn từ đơn hàng cũ vào giỏ hàng mới của khách hàng.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng (Nút Reorder)
    participant C as OrderController<br>(Presentation)
    participant UC as ReorderPastOrderUseCaseImpl<br>(Application)
    participant OR as OrderJpaRepository<br>(Infrastructure)
    participant OIR as OrderItemJpaRepository<br>(Infrastructure)
    participant PR as ProductRepository<br>(Infrastructure)
    participant CS as CartService<br>(Application)

    Client->>C: POST /api/v1/orders/{orderId}/reorder
    activate C
    C->>UC: execute(tenantId, userId, orderId)
    activate UC

    UC->>OR: findByIdAndTenantId(orderId, tenantId)
    OR-->>UC: Past OrderJpaEntity

    UC->>OIR: findByOrderId(orderId)
    OIR-->>UC: List<OrderItemJpaEntity>

    loop Cho từng món trong đơn cũ
        UC->>PR: findByIdAndTenantId(productId, tenantId)
        PR-->>UC: ProductJpaEntity (Kiểm tra còn kinh doanh không)
        UC->>CS: addItem(tenantId, userId, AddCartItemRequest)
        CS-->>UC: Updated Cart
    end

    UC-->>C: ReorderResponse
    deactivate UC
    C-->>Client: 200 OK: ApiResponse.ok(MSG_REORDER_SUCCESS, ReorderResponse)
    deactivate C
```

---

### UC-6.10: Gửi Biên Nhận Điện Tử E-Receipt qua Email (Send E-Receipt PDF)
**Endpoint:** `POST /api/v1/orders/{orderId}/receipt/send-email`  
**Mô tả:** Sinh file hóa đơn điện tử PDF chuẩn (PDF 1.4 byte stream) và gửi đính kèm tới địa chỉ Email của khách qua JavaMailSender.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng (Popup E-Receipt)
    participant C as OrderController<br>(Presentation)
    participant UC as SendEReceiptEmailUseCaseImpl<br>(Application)
    participant OR as OrderJpaRepository<br>(Infrastructure)
    participant OIR as OrderItemJpaRepository<br>(Infrastructure)
    participant MailAdapter as JavaMailReceiptAdapter<br>(Infrastructure)
    participant MailSender as JavaMailSender (SMTP)

    Client->>C: POST /api/v1/orders/{orderId}/receipt/send-email { email: "info@ordera.no" }
    activate C
    C->>UC: execute(tenantId, userId, orderId, email)
    activate UC

    UC->>OR: findByIdAndTenantId(orderId, tenantId)
    OR-->>UC: OrderJpaEntity

    UC->>OIR: findByOrderId(orderId)
    OIR-->>UC: List<OrderItemJpaEntity>

    Note over UC: Cập nhật eReceiptEmail trên đơn hàng
    UC->>OR: save(order.setEReceiptEmail(email))

    UC->>MailAdapter: sendReceiptEmail(order, items, email)
    activate MailAdapter
    Note over MailAdapter: Sinh byte[] PDF 1.4 chuẩn hóa đơn<br>(Logo, Mã đơn, Thuế VAT 15%, Tips, Điểm thưởng)
    MailAdapter->>MailSender: send(MimeMessage: HTML body + PDF attachment)
    MailSender-->>MailAdapter: Sent OK
    MailAdapter-->>UC: Completed
    deactivate MailAdapter

    UC-->>C: SendEReceiptResponse
    deactivate UC
    C-->>Client: 200 OK: ApiResponse.ok(MSG_RECEIPT_EMAIL_SENT, SendEReceiptResponse)
    deactivate C
```

