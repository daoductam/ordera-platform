# Tài Liệu Luồng Nghiệp Vụ & Kiến Trúc Phân Hệ Kiosk POS (Business Flows & Architecture)

> **Tài liệu đặc tả các luồng nghiệp vụ và tương tác 4 lớp Clean Architecture**  
> **Ánh xạ Use Cases:** 7.1 Tra cứu SĐT, 7.2 Tích điểm nhanh, 7.3 Tự đặt món, 7.4 Reset phiên 60s, 7.5 Cấu hình thiết bị trạm  
> **Module Backend:** `com.ordera.modules.kiosk`  

---

## 1. Sơ Đồ Tổng Quan Trạng Thái & Điều Khiển Trạm Kiosk (Kiosk State Machine Flow)

```mermaid
flowchart TD
    STANDBY["STANDBY Màn hình chờ Kiosk<br/>(Dine in / Take away / Loyalty Numpad)"]

    subgraph FLOW_IDENTIFY ["7.1 Luồng Nhận Diện Khách Hàng"]
        NUMPAD["Khách bấm SĐT trên bàn phím số Numpad"]
        LOOKUP["Tra cứu User, Hạng thẻ & Voucher"]
        GUEST_PROMPT{"Đã là hội viên?"}
        SHOW_MEMBER["Hiển thị Tên, Tier, Điểm & Tỷ lệ hoàn"]
        SHOW_GUEST["Hiển thị Khách mới<br/>(Cho phép bấm Skip hoặc Đăng ký)"]
    end

    subgraph FLOW_QUICK_EARN ["7.2 Luồng Tích Điểm Nhanh Tại Quầy (POS Ngoài)"]
        RECEIPT_INPUT["Thu ngân xuất hóa đơn POS ngoài"]
        ACC_POINTS["Tính điểm theo hạng thẻ hội viên"]
        POINTS_LEDGER[("Ghi sổ cái bất biến points_ledger<br/>Type: KIOSK_EARN")]
        PRINT_TICKET["In phiếu nhiệt & Sinh số thứ tự #A-08"]
    end

    subgraph FLOW_CHECKOUT ["7.3 Luồng Tự Đặt Món Trực Tiếp Tại Kiosk"]
        CART_SELECT["Khách chọn món & Tips"]
        CARD_PAY["Thanh toán máy quẹt thẻ POS tích hợp"]
        CREATE_ORDER[("Lưu đơn hàng orders & order_items<br/>Status: PAID")]
        PRINT_ORDER["In phiếu chế biến bếp & Số thứ tự #A-08"]
    end

    subgraph FLOW_WATCHDOG ["7.4 Luồng Giám Sát Phiên 60 Giây (Idle Watchdog)"]
        TIMER["Bộ đếm ngược 60 giây không tương tác"]
        TOUCH_RESET["Khách chạm màn hình -> Reset về 60s"]
        TIMEOUT_EXPIRED["Hết 60s hoặc bấm nút Start Over"]
        CLEAR_CACHE[("Xóa trắng session cache Redis")]
    end

    STANDBY --> NUMPAD
    NUMPAD --> LOOKUP
    LOOKUP --> GUEST_PROMPT
    GUEST_PROMPT -- "Có (isRegistered = true)" --> SHOW_MEMBER
    GUEST_PROMPT -- "Chưa (isRegistered = false)" --> SHOW_GUEST

    SHOW_MEMBER --> CART_SELECT
    SHOW_GUEST --> CART_SELECT

    CART_SELECT --> CARD_PAY
    CARD_PAY --> CREATE_ORDER
    CREATE_ORDER --> PRINT_ORDER
    PRINT_ORDER --> CLEAR_CACHE

    STANDBY --> RECEIPT_INPUT
    RECEIPT_INPUT --> ACC_POINTS
    ACC_POINTS --> POINTS_LEDGER
    POINTS_LEDGER --> PRINT_TICKET
    PRINT_TICKET --> CLEAR_CACHE

    STANDBY -.-> TIMER
    TIMER -- "Có chạm màn hình" --> TOUCH_RESET
    TIMER -- "Không tương tác 60s" --> TIMEOUT_EXPIRED
    TIMEOUT_EXPIRED --> CLEAR_CACHE
    CLEAR_CACHE --> STANDBY
```

---

## 2. Sequence Diagrams 4 Lớp Clean Architecture (5 Use Cases)

### 2.1. Use Case 7.1: Tra Cứu & Nhận Diện Khách Hàng Qua Số Điện Thoại (`KioskIdentifyCustomerUseCase`)

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng (Kiosk Numpad)
    participant UI as Kiosk Touch Client
    participant Controller as KioskController
    participant UseCase as KioskIdentifyCustomerUseCaseImpl
    participant UserRepo as UserRepository
    participant LoyaltyRepo as MemberProfile & Tier Repo
    participant VoucherRepo as UserVoucherJpaRepository
    participant RedisAdapter as RedisKioskSessionAdapter

    Customer->>UI: Chạm SĐT trên màn cảm ứng (+4791234567)
    UI->>Controller: POST /api/v1/kiosk/identify<br/>Headers: X-Tenant-ID, X-Kiosk-Device-ID
    Controller->>UseCase: execute(tenantId, deviceId, request)
    
    UseCase->>UseCase: validateInputs() & validateKioskDevice()
    UseCase->>UserRepo: findByTenantIdAndPhoneNumber(tenantId, phone)
    
    alt Hội viên đã đăng ký trong hệ thống
        UserRepo-->>UseCase: UserJpaEntity
        UseCase->>LoyaltyRepo: findByIdAndTenantId(userId, tenantId)
        LoyaltyRepo-->>UseCase: MemberProfile & TierDefinition (Tên tier, multiplier)
        UseCase->>VoucherRepo: countByTenantIdAndMemberIdAndStatus(tenantId, userId, "ACTIVE")
        VoucherRepo-->>UseCase: availableVoucherCount
        UseCase->>RedisAdapter: saveSession(tenantId, deviceId, session)
        UseCase-->>Controller: KioskCustomerInfoResponse (isRegistered=true, tier, points, vouchers)
    else Khách mới chưa đăng ký (Guest)
        UserRepo-->>UseCase: Optional.empty()
        UseCase->>RedisAdapter: saveSession(tenantId, deviceId, guestSession)
        UseCase-->>Controller: KioskCustomerInfoResponse (isRegistered=false, name="Khách tích điểm mới")
    end

    Controller-->>UI: 200 OK (ApiResponse.ok(MSG_CUSTOMER_IDENTIFIED, data))
    UI-->>Customer: Hiển thị màn hình chào hội viên / nút Skip
```

---

### 2.2. Use Case 7.2: Tích Điểm Nhanh Tại Quầy Không Cần Đặt Món (`KioskQuickEarnUseCase`)

```mermaid
sequenceDiagram
    autonumber
    actor Staff as Thu Ngân & Khách Hàng
    participant UI as Kiosk Màn Hình Phụ
    participant Controller as KioskController
    participant UseCase as KioskQuickEarnUseCaseImpl
    participant UserRepo as UserRepository
    participant AccrueEngine as AccruePointsUseCase
    participant QueueUtils as KioskQueueUtils
    participant Printer as ThermalPrinterAdapter

    Staff->>UI: Khách nhập SĐT, Thu ngân truyền billAmount (93 NOK)
    UI->>Controller: POST /api/v1/kiosk/quick-earn<br/>Headers: X-Tenant-ID, X-Kiosk-Device-ID, X-Staff-PIN
    Controller->>UseCase: execute(tenantId, deviceId, staffPin, request)
    
    UseCase->>UseCase: validateKioskDevice() & checkBillAmount >= 1.00
    UseCase->>UserRepo: lookupCustomerByPhone(tenantId, phoneNumber)
    UserRepo-->>UseCase: UserJpaEntity
    UseCase->>UseCase: calculateEarnPoints(earnRate, billAmount)
    
    UseCase->>AccrueEngine: execute(AccruePointsCommand: KIOSK_EARN, pointsDelta)
    Note over AccrueEngine: Chiếm Redisson Distributed Lock,<br/>Ghi sổ cái bất biến points_ledger,<br/>Tự động thăng hạng thẻ nếu đạt mốc
    AccrueEngine-->>UseCase: PointsLedgerJpaEntity (balanceAfter, txId)
    
    UseCase->>QueueUtils: generateQueueNumber(redissonClient, storeId)
    QueueUtils-->>UseCase: Số thứ tự "#A-08"
    
    UseCase->>Printer: printQuickEarnReceipt(storeId, "#A-08", receiptRef, billAmount, points)
    Note over Printer: Xuất lệnh in nhiệt phiếu tích điểm
    
    UseCase-->>Controller: KioskQuickEarnResponse (pointsEarned, newTotalPoints, queueNumber)
    Controller-->>UI: 201 Created (ApiResponse.ok(MSG_QUICK_POINTS_EARNED, data))
    UI-->>Staff: Hiển thị màn hình thành công & in phiếu tại quầy
```

---

### 2.3. Use Case 7.3: Khách Tự Đặt Món & Thanh Toán Trực Tiếp (`CreateKioskOrderUseCase`)

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng
    participant UI as Kiosk Self-Ordering Screen
    participant Controller as KioskController
    participant UseCase as CreateKioskOrderUseCaseImpl
    participant CatalogRepo as ProductRepository
    participant OrderRepo as OrderJpaRepository & ItemRepo
    participant AccrueEngine as AccruePointsUseCase
    participant QueueUtils as KioskQueueUtils
    participant Printer as ThermalPrinterAdapter
    participant RedisAdapter as RedisKioskSessionAdapter

    Customer->>UI: Chọn món, tiền tip (0 NOK) & chạm [Pay with Card]
    UI->>Controller: POST /api/v1/kiosk/checkout<br/>Headers: X-Tenant-ID, X-Kiosk-Device-ID
    Controller->>UseCase: execute(tenantId, deviceId, request)
    
    UseCase->>UseCase: validateKioskCheckout()
    UseCase->>CatalogRepo: findByIdAndTenantId(productId, tenantId)
    CatalogRepo-->>UseCase: ProductJpaEntity (kiểm tra available, đơn giá)
    UseCase->>UseCase: calculateFinancials(subtotal, vat 15%, tip, total)
    
    opt Khách có tài khoản hội viên
        UseCase->>AccrueEngine: execute(AccruePointsCommand: KIOSK_EARN)
        AccrueEngine-->>UseCase: Points credited
    end
    
    UseCase->>QueueUtils: generateQueueNumber(redissonClient, storeId)
    QueueUtils-->>UseCase: "#A-08" & mã đơn "A-08"
    
    UseCase->>OrderRepo: save(OrderJpaEntity & OrderItemJpaEntity)
    OrderRepo-->>UseCase: Order persisted (status=PAID)
    
    UseCase->>Printer: printQueueTicket(storeId, "#A-08", "A-08")
    UseCase->>RedisAdapter: clearSession(tenantId, deviceId)
    
    UseCase-->>Controller: KioskCheckoutResponse (orderId, queueNumber="#A-08", resetTimeout=5s)
    Controller-->>UI: 201 Created (ApiResponse.ok(MSG_KIOSK_ORDER_PLACED, data))
    UI-->>Customer: Màn hình đặt món thành công, xuất bill nhiệt & đếm ngược 5s
```

---

### 2.4. Use Case 7.4: Reset Phiên Làm Việc Kiosk Về Màn Hình Chờ (`ResetKioskSessionUseCase`)

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng / Watchdog Timer
    participant UI as Kiosk Front-End
    participant Controller as KioskController
    participant UseCase as ResetKioskSessionUseCaseImpl
    participant RedisAdapter as RedisKioskSessionAdapter

    alt Khách chủ động nhấn nút [Start Over]
        Customer->>UI: Bấm nút [Start Over]
    else Khách rời đi không tương tác quá 60 giây
        Customer->>UI: Đồng hồ đếm ngược hết giờ (countdown <= 0)
    end

    UI->>Controller: POST /api/v1/kiosk/session/reset<br/>Headers: X-Tenant-ID, X-Kiosk-Device-ID<br/>Body: {storeId, reason: "IDLE_TIMEOUT"}
    Controller->>UseCase: execute(tenantId, deviceId, request)
    
    UseCase->>UseCase: validateInputs()
    UseCase->>RedisAdapter: clearSession(tenantId, deviceId)
    Note over RedisAdapter: Xóa toàn bộ key cache phiên trên Redis
    
    UseCase-->>Controller: KioskResetSessionResponse (kioskStatus="STANDBY", countdownDefault=60s)
    Controller-->>UI: 200 OK (ApiResponse.ok(MSG_SESSION_RESET, data))
    UI-->>Customer: Trở về màn hình chờ ban đầu K01.01
```

---

### 2.5. Use Case 7.5: Cấu Hình Thiết Bị & Khởi Tạo Trạm Kiosk (`GetKioskConfigUseCase`)

```mermaid
sequenceDiagram
    autonumber
    actor Terminal as Trạm Kiosk Khởi Động / Heartbeat
    participant UI as Kiosk Front-End
    participant Controller as KioskController
    participant UseCase as GetKioskConfigUseCaseImpl
    participant KioskRepo as KioskDeviceJpaRepository
    participant StoreRepo as StoreRepository
    participant Mapper as KioskDtoMapper

    Terminal->>UI: Khởi động trình duyệt Kiosk cảm ứng
    UI->>Controller: GET /api/v1/kiosk/config/{deviceId}<br/>Header: X-Tenant-ID
    Controller->>UseCase: execute(tenantId, deviceId)
    
    UseCase->>UseCase: validateInputs()
    UseCase->>KioskRepo: findByTenantIdAndDeviceCode(tenantId, deviceId)
    alt Không tìm thấy thiết bị
        KioskRepo-->>UseCase: Optional.empty()
        UseCase-->>Controller: ném BusinessException(KIOSK_DEVICE_NOT_FOUND)
        Controller-->>UI: 404 Not Found (RFC 7807 Problem Details)
    else Tìm thấy thiết bị
        KioskRepo-->>UseCase: KioskDeviceJpaEntity
        UseCase->>KioskRepo: save(device với lastHeartbeatAt = now())
        UseCase->>StoreRepo: findByIdAndTenantId(storeId, tenantId)
        StoreRepo-->>UseCase: StoreJpaEntity
        UseCase->>Mapper: toConfigResponse(device, store)
        Mapper-->>UseCase: KioskConfigResponse (idleTimeout=60, printerStatus, cardReaderStatus)
        UseCase-->>Controller: KioskConfigResponse
        Controller-->>UI: 200 OK (ApiResponse.ok(MSG_KIOSK_CONFIG_RETRIEVED, data))
        UI-->>Terminal: Khởi tạo giao diện theo cấu hình chi nhánh
    end
```

---

## 3. Bảng Kiểm Tra An Ninh & Phân Lập Đa Thương Hiệu (Security & Multi-Tenant Guard)

| Tiêu chí | Cơ chế thực thi trong Kiosk Module |
|---|---|
| **Multi-Tenant Isolation** | Header bắt buộc `X-Tenant-ID`, giải quyết qua `TenantContextHolder`, mọi câu lệnh JPA đều chứa `tenant_id` |
| **Thiết bị hợp lệ** | Header bắt buộc `X-Kiosk-Device-ID`, đối soát duy nhất theo cặp `(tenant_id, device_code)` |
| **Bảo vệ mật mã thu ngân** | Header `X-Staff-PIN` bảo vệ thao tác tích điểm nhanh tại quầy |
| **Chống Race Condition** | Khóa phân tán Redisson Distributed Lock cho phiên giao dịch và tích lũy điểm thưởng |
| **Sổ cái điểm kép bất biến** | Không cho phép cập nhật điểm thủ công, toàn bộ giao dịch được ghi nối đuôi vào bảng phân vùng `points_ledger` |

