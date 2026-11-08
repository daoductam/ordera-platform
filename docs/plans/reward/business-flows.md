# Phân Hệ Ví Voucher, Đổi Thưởng & Ưu Đãi Giỏ Hàng — Sơ Đồ Nghiệp Vụ & Kiến Trúc (Business Flows & Architecture)

> **Tài liệu đặc tả luồng xử lý và kiến trúc Clean Architecture 4 lớp cho Phân hệ Reward & Voucher**  
> **Ánh xạ Prototype:** Figma Nhóm 07 (`07.01` - `07.08`), Figma Nhóm 08 (`08.01` - `08.12`)  
> **Chuẩn hiển thị:** Dark Mode & Clean Architecture Tracing

---

## 1. Toàn Cảnh Nghiệp Vụ Phân Hệ (Business Workflow Flowchart)

```mermaid
flowchart TD
    classDef client fill:#1E293B,stroke:#38BDF8,stroke-width:2px,color:#F8FAFC;
    classDef process fill:#0F172A,stroke:#818CF8,stroke-width:2px,color:#F8FAFC;
    classDef lock fill:#450A0A,stroke:#F87171,stroke-width:2px,color:#FECACA;
    classDef decision fill:#312E81,stroke:#C084FC,stroke-width:2px,color:#F8FAFC;
    classDef success fill:#064E3B,stroke:#34D399,stroke-width:2px,color:#ECFDF5;

    User([Hội Viên Ordera]):::client --> Choice{Chọn Thao Tác}:::decision

    %% Luồng 1: Ví của tôi & Catalog
    Choice -->|Xem Ví Ưu Đãi| Wallet[GetMyRewardsWalletUseCase]:::process
    Wallet --> RenderWallet[Hiển thị Vouchers & Quà Hiện Vật trong ví]:::success

    Choice -->|Xem Kho Đổi Thưởng| Catalog[GetRewardsCatalogUseCase]:::process
    Catalog --> EvaluatePoints[So khớp Điểm khả dụng & canAfford]:::process
    EvaluatePoints --> RenderCatalog[Hiển thị Mốc Đổi 100 pts / 1000 pts]:::success

    %% Luồng 2: Đổi Voucher
    Choice -->|Đổi Voucher| RedeemVoucher[RedeemVoucherUseCase]:::process
    RedeemVoucher --> LockMember[Acquire Redisson Lock: lock:member]:::lock
    LockMember --> CheckBalanceVoucher{Số dư điểm >= pointsCost?}:::decision
    CheckBalanceVoucher -->|Không đủ| InsufficientPoints[Throw 422 INSUFFICIENT_POINTS]:::lock
    CheckBalanceVoucher -->|Đủ điểm| DeductLedgerVoucher[AccruePointsUseCase: VOUCHER_REDEEM]:::process
    DeductLedgerVoucher --> CreateUserVoucher[Tạo user_vouchers: AVAILABLE]:::success
    CreateUserVoucher --> ReleaseLockVoucher[Release Redisson Lock]:::lock

    %% Luồng 3: Đổi Quà Tumbler
    Choice -->|Đổi Cốc Tumbler| RedeemGift[RedeemPhysicalGiftUseCase]:::process
    RedeemGift --> MultiLock[Acquire Redisson MultiLock: member + gift]:::lock
    MultiLock --> CheckStock{stock_quantity > 0 & Đủ điểm?}:::decision
    CheckStock -->|Hết hàng| OutOfStock[Throw 409 GIFT_OUT_OF_STOCK]:::lock
    CheckStock -->|Hợp lệ| DecrementStock[Giảm tồn kho: stock_quantity - 1]:::process
    DecrementStock --> GenCode[Sinh mã nhận quà 8 ký tự]:::process
    GenCode --> DeductLedgerGift[AccruePointsUseCase: GIFT_REDEEM]:::process
    DeductLedgerGift --> CreateRedemption[Tạo user_gift_redemptions: PENDING]:::success
    CreateRedemption --> ReleaseMultiLock[Release Redisson MultiLock]:::lock

    %% Luồng 4: Ưu Đãi Giỏ Hàng
    Choice -->|Mở Drawer Checkout| Drawer[GetAvailableOffersForCartUseCase]:::process
    Drawer --> CheckSubtotal[Đánh giá subtotalAmount >= minOrderValue]:::decision
    CheckSubtotal --> RenderDrawer[Hiển thị Voucher & Quà khả dụng]:::success

    Choice -->|Áp Dụng Mã Giảm Giá| ApplyVoucher[ApplyVoucherToCartUseCase]:::process
    ApplyVoucher --> ComboRuleVoucher{Giỏ hàng đã có voucher khác?}:::decision
    ComboRuleVoucher -->|Đã có voucher| RejectComboVoucher[Throw 422 INVALID_REWARD_COMBINATION]:::lock
    ComboRuleVoucher -->|Chưa có voucher| LockVoucher[Đổi user_vouchers sang IN_USE]:::process
    LockVoucher --> RecalculateCart[Gán vào giỏ & Tính lại tổng tiền sau giảm]:::success

    Choice -->|Áp Dụng Quà Hiện Vật| ApplyGift[ApplyGiftToCartUseCase]:::process
    ApplyGift --> ComboRuleGift{Giỏ hàng đã có quà khác?}:::decision
    ComboRuleGift -->|Đã có quà| RejectComboGift[Throw 422 INVALID_REWARD_COMBINATION]:::lock
    ComboRuleGift -->|Chưa có quà| SelectGift[Đổi user_gift_redemptions sang SELECTED]:::process
    SelectGift --> AttachGiftCart[Gán applied_gift_id vào giỏ hàng]:::success
```

---

## 2. Sequence Diagram 1: Đổi Điểm Lấy Mã Voucher (API 5.3)

Biểu đồ tuần tự Clean Architecture mô tả luồng đổi voucher với cơ chế khóa phân tán Redisson và ghi sổ cái điểm kép bất biến.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Khách Hàng (PWA Mobile)
    participant Ctrl as RewardController
    participant UC as RedeemVoucherUseCaseImpl
    participant Lock as RedissonClient (Redis)
    participant VRepo as VoucherJpaRepository
    participant MRepo as MemberProfileJpaRepository
    participant Accrue as AccruePointsUseCase
    participant UVRepo as UserVoucherJpaRepository
    participant DB as PostgreSQL Database

    Client->>Ctrl: POST /api/v1/rewards/vouchers/{voucherId}/redeem<br/>(Header: X-Tenant-ID, Authorization)
    Ctrl->>UC: execute(voucherId)
    
    Note over UC,Lock: 1. Chiếm giữ Khóa phân tán tránh race condition bấm đúp
    UC->>Lock: getLock("lock:tenant:{id}:reward:member:{id}").tryLock(3s, 5s)
    Lock-->>UC: lockAcquired = true

    critical Transaction Boundary
        UC->>VRepo: findByIdAndTenantId(voucherId, tenantId)
        VRepo->>DB: SELECT * FROM vouchers WHERE id = ? AND tenant_id = ?
        DB-->>VRepo: VoucherJpaEntity
        VRepo-->>UC: voucher

        UC->>MRepo: findByTenantIdAndUserId(tenantId, userId)
        MRepo->>DB: SELECT * FROM member_profiles WHERE user_id = ?
        DB-->>MRepo: MemberProfileJpaEntity
        MRepo-->>UC: memberProfile

        Note over UC: Thẩm định: voucher.active, validUntil & currentPoints >= pointsCost

        Note over UC,Accrue: 2. Ghi sổ cái bất biến & cập nhật số dư điểm
        UC->>Accrue: execute(AccruePointsCommand[pointsDelta = -cost, type = VOUCHER_REDEEM])
        Accrue->>DB: INSERT INTO points_ledger (...)
        Accrue->>DB: UPDATE member_profiles SET current_points = newPoints
        Accrue-->>UC: PointsLedgerJpaEntity (balanceAfter)

        Note over UC,UVRepo: 3. Thêm voucher vào ví cá nhân
        UC->>UVRepo: save(UserVoucherJpaEntity[status = AVAILABLE])
        UVRepo->>DB: INSERT INTO user_vouchers (...)
        DB-->>UVRepo: savedUserVoucher
        UVRepo-->>UC: userVoucher
    end

    Note over UC,Lock: 4. Giải phóng Redisson Lock trong khối finally
    UC->>Lock: unlock()

    UC-->>Ctrl: RedeemVoucherResponse
    Ctrl-->>Client: 201 Created (ApiResponse.ok(MSG_VOUCHER_REDEEMED, data))
```

---

## 3. Sequence Diagram 2: Đổi Quà Hiện Vật Cốc Tumbler (API 5.4)

Biểu đồ mô tả quy trình Redisson Multi-Lock đồng thời kiểm tra tồn kho vật lý và số dư điểm để phòng chống bán vượt tồn kho.

```mermaid
sequenceDiagram
    autonumber
    actor Client as Khách Hàng (PWA Mobile)
    participant Ctrl as RewardController
    participant UC as RedeemPhysicalGiftUseCaseImpl
    participant Lock as RedissonClient (Redis)
    participant GRepo as LoyaltyGiftJpaRepository
    participant MRepo as MemberProfileJpaRepository
    participant Accrue as AccruePointsUseCase
    participant UGRepo as UserGiftRedemptionJpaRepository
    participant DB as PostgreSQL Database

    Client->>Ctrl: POST /api/v1/rewards/gifts/{giftId}/redeem<br/>(Header: X-Tenant-ID, Authorization)
    Ctrl->>UC: execute(giftId)

    Note over UC,Lock: Chiếm giữ Redisson Multi-Lock đồng thời trên Member và Gift
    UC->>Lock: getMultiLock(memberLock, giftLock).tryLock(3s, 5s)
    Lock-->>UC: multiLockAcquired = true

    critical Transaction Boundary
        UC->>GRepo: findByIdAndTenantId(giftId, tenantId)
        GRepo->>DB: SELECT * FROM loyalty_gifts WHERE id = ?
        DB-->>GRepo: LoyaltyGiftJpaEntity
        GRepo-->>UC: gift

        Note over UC: Kiểm tra: gift.stockQuantity > 0 & member.currentPoints >= pointsCost
        
        Note over UC,GRepo: 1. Trừ 1 số lượng tồn kho vật phẩm
        UC->>GRepo: save(gift[stockQuantity = stockQuantity - 1])
        GRepo->>DB: UPDATE loyalty_gifts SET stock_quantity = stock_quantity - 1

        Note over UC,Accrue: 2. Ghi sổ cái trừ điểm
        UC->>Accrue: execute(AccruePointsCommand[pointsDelta = -pointsCost, type = GIFT_REDEEM])
        Accrue->>DB: INSERT INTO points_ledger & UPDATE member_profiles
        Accrue-->>UC: PointsLedgerJpaEntity

        Note over UC,UGRepo: 3. Sinh mã nhận quà 8 ký tự & tạo bản ghi nhận quà
        UC->>UGRepo: save(UserGiftRedemptionJpaEntity[code = 8-chars, status = PENDING])
        UGRepo->>DB: INSERT INTO user_gift_redemptions (...)
        DB-->>UGRepo: savedRedemption
        UGRepo-->>UC: redemption
    end

    Note over UC,Lock: Giải phóng Multi-Lock trong khối finally
    UC->>Lock: unlock()

    UC-->>Ctrl: RedeemGiftResponse
    Ctrl-->>Client: 201 Created (ApiResponse.ok(MSG_GIFT_REDEEMED, data))
```

---

## 4. Sequence Diagram 3: Áp Dụng Mã Voucher Vào Giỏ Hàng (API 5.8)

Mô tả sự phối hợp giữa UseCase, Port & Adapter Giỏ Hàng, và bảo toàn quy tắc kết hợp (Combination Rules).

```mermaid
sequenceDiagram
    autonumber
    actor Client as Khách Hàng (Drawer Checkout)
    participant Ctrl as CartOfferController
    participant UC as ApplyVoucherToCartUseCaseImpl
    participant Port as CartOfferPort (CartOfferAdapter)
    participant UVRepo as UserVoucherJpaRepository
    participant VRepo as VoucherJpaRepository
    participant DB as PostgreSQL Database

    Client->>Ctrl: POST /api/v1/cart/voucher/apply<br/>Body: { cartId, voucherCode }
    Ctrl->>UC: execute(ApplyVoucherRequest)

    UC->>Port: findCartByIdAndTenantId(cartId, tenantId)
    Port->>DB: SELECT * FROM carts & cart_items WHERE id = ?
    DB-->>Port: CartJpaEntity & Items
    Port-->>UC: CartOfferDetails (subtotalAmount, appliedVoucherCode)

    Note over UC: Kiểm tra Quy Tắc Kết Hợp: Giỏ hàng chỉ được có tối đa 01 Voucher

    UC->>VRepo: findByTenantIdAndCode(tenantId, voucherCode)
    VRepo-->>UC: VoucherJpaEntity (minOrderValue, discountValue)

    Note over UC: Kiểm tra điều kiện đơn tối thiểu: subtotalAmount >= minOrderValue

    UC->>UVRepo: findByTenantIdAndMemberIdAndVoucherIdAndStatus(AVAILABLE)
    UVRepo-->>UC: UserVoucherJpaEntity

    critical Update Cart & Voucher State
        Note over UC,UVRepo: 1. Khóa voucher sang IN_USE
        UC->>UVRepo: save(userVoucher[status = IN_USE, appliedOrderId = cartId])
        UVRepo->>DB: UPDATE user_vouchers SET status = 'IN_USE'

        Note over UC,Port: 2. Gán voucher vào giỏ hàng
        UC->>Port: updateAppliedVoucher(cartId, tenantId, voucherCode)
        Port->>DB: UPDATE carts SET applied_voucher_code = ?
    end

    Note over UC: 3. Tính toán lại bảng chi phí: subtotal, discount, vat, finalTotal

    UC-->>Ctrl: CartVoucherCalculationResponse
    Ctrl-->>Client: 200 OK (ApiResponse.ok(MSG_VOUCHER_APPLIED, data))
```

---

## 5. Sequence Diagram 4: Gỡ Bỏ Voucher Khỏi Giỏ Hàng (API 5.9)

```mermaid
sequenceDiagram
    autonumber
    actor Client as Khách Hàng
    participant Ctrl as CartOfferController
    participant UC as RemoveVoucherFromCartUseCaseImpl
    participant Port as CartOfferPort
    participant UVRepo as UserVoucherJpaRepository
    participant DB as PostgreSQL Database

    Client->>Ctrl: DELETE /api/v1/cart/voucher/remove<br/>Body: { cartId, voucherCode }
    Ctrl->>UC: execute(RemoveVoucherRequest)

    UC->>Port: findCartByIdAndTenantId(cartId, tenantId)
    Port-->>UC: CartOfferDetails

    Note over UC: Xác nhận voucher đang được gán cho giỏ hàng này

    critical Release Voucher & Update Cart
        UC->>UVRepo: findByAppliedOrderId(cartId)
        UVRepo-->>UC: userVoucher
        Note over UC,UVRepo: Hoàn trả voucher về trạng thái AVAILABLE
        UC->>UVRepo: save(userVoucher[status = AVAILABLE, appliedOrderId = null])
        UVRepo->>DB: UPDATE user_vouchers SET status = 'AVAILABLE', applied_order_id = null

        UC->>Port: clearAppliedVoucher(cartId, tenantId)
        Port->>DB: UPDATE carts SET applied_voucher_code = null
    end

    UC-->>Ctrl: CartVoucherCalculationResponse (discount = 0)
    Ctrl-->>Client: 200 OK (ApiResponse.ok(MSG_VOUCHER_REMOVED, data))
```

---

## 6. Tổng Kết Quy Tắc Kết Hợp & Trạng Thái (State Machines)

### Vòng đời trạng thái Voucher (`user_vouchers.status`)
```mermaid
stateDiagram-v2
    [*] --> AVAILABLE : Đổi điểm thành công (API 5.3)
    AVAILABLE --> IN_USE : Áp dụng vào giỏ hàng (API 5.8)
    IN_USE --> AVAILABLE : Gỡ khỏi giỏ hàng (API 5.9) / Hủy giỏ
    IN_USE --> USED : Thanh toán đơn hàng thành công (Order Checkout)
    AVAILABLE --> EXPIRED : Hết hạn hiệu lực (Cron Job)
    USED --> [*]
    EXPIRED --> [*]
```

### Vòng đời trạng thái Quà Hiện Vật (`user_gift_redemptions.status`)
```mermaid
stateDiagram-v2
    [*] --> PENDING : Đổi quà tại kho thưởng (API 5.4)
    PENDING --> SELECTED : Chọn nhận kèm trong giỏ hàng (API 5.10)
    SELECTED --> PENDING : Bỏ chọn khỏi giỏ hàng (API 5.11)
    SELECTED --> CLAIMED : Nhận quà cùng đơn hàng thành công
    PENDING --> CLAIMED : Xuất trình mã tại quầy thu ngân quét nhận quà
    CLAIMED --> [*]
```
