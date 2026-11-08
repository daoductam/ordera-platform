# Business Flows & Architecture Sequences: Loyalty & Immutable Points Ledger Engine

Tài liệu thiết kế luồng nghiệp vụ (Business Flowcharts) và biểu đồ tương tác 4 lớp Clean Architecture (Sequence Diagrams) cho Phân hệ Hồ sơ Khách hàng, 6 Cấp bậc Thẻ (New $\rightarrow$ Ruby), Động cơ Sổ cái Điểm Bất biến (Points Ledger) & Khiếu nại Thiếu điểm Hóa đơn.

---

## 1. Tổng quan Luồng Nghiệp vụ Loyalty & Sổ cái Điểm (End-to-End Business Flowchart)

Biểu đồ Flowchart tổng thể mô tả hành trình từ lúc khách hàng tra cứu thẻ thành viên, trình mã Barcode tích điểm tại quầy/Kiosk, động cơ khóa phân tán Redisson thực thi ghi nhận sổ cái điểm bất biến và tự động thăng hạng thẻ, đến luồng khiếu nại bù điểm hóa đơn bị sót.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#94a3b8', 'secondaryColor': '#0f172a', 'tertiaryColor': '#1e293b'}}}%%
flowchart TD
    Client(["Khách hàng (Mobile Web PWA) / Kiosk POS"])

    subgraph MemberFlow["Luồng Hồ Sơ & Thẻ Thành Viên"]
        ViewProfile["1. Xem Hồ sơ & Thẻ Thành viên<br>(Current Points, Lifetime Points, 6 Tiers)"]
        BarcodeScan["2. Xuất trình Barcode Token quét tại quầy<br>(vd: ORD-MEM-97362748)"]
        UpdateInfo["3. Cập nhật thông tin & Upload Avatar"]
    end

    subgraph AccrualEngine["Động cơ Sổ Cái Điểm & Thăng Hạng Tự Động"]
        TriggerAccrual{"Giao dịch biến động điểm<br>(ORDER_EARN, REDEEM, KIOSK)"}
        AcquireLock["4. Chiếm giữ Redisson Lock<br>(lock:tenant:member)"]
        CheckBalance{"Kiểm tra số dư<br>(nếu trừ điểm)?"}
        InsertLedger["5. INSERT bản ghi mới vào points_ledger<br>(Immutability - Cấm sửa xóa)"]
        UpdateProfileCache["6. Cập nhật current_points & lifetime_points cache"]
        EvaluateTier{"Đủ điều kiện<br>thăng hạng thẻ?"}
        UpgradeTier["7. Thăng hạng thẻ mới<br>(Silver 200, Gold 400, Platinum 1000, Diamond 2000, Ruby 4000)"]
        ReleaseLock["8. Giải phóng Redisson Lock"]
    end

    subgraph ClaimFlow["Luồng Khiếu Nại Thiếu Điểm Hóa Đơn"]
        SubmitClaim["9. Khách gửi khiếu nại hóa đơn<br>(Mã hóa đơn, Ngày mua, Số tiền, Ảnh biên lai)"]
        ValidateClaim{"Hóa đơn trong vòng 30 ngày<br>& chưa từng tích điểm?"}
        CreateTicket["10. Tạo ticket support_claims (PENDING)"]
        AdminReview["11. Quản trị viên duyệt hóa đơn"]
        CreditPoints["12. Kích hoạt AccruePointsUseCase (CLAIM_CREDIT)"]
    end

    Client --> ViewProfile
    Client --> BarcodeScan
    Client --> UpdateInfo
    BarcodeScan --> TriggerAccrual
    TriggerAccrual --> AcquireLock
    AcquireLock --> CheckBalance
    CheckBalance -->|Hợp lệ| InsertLedger
    CheckBalance -->|Không đủ điểm| ReleaseLock
    InsertLedger --> UpdateProfileCache
    UpdateProfileCache --> EvaluateTier
    EvaluateTier -->|Đạt mốc điểm| UpgradeTier
    EvaluateTier -->|Chưa đạt| ReleaseLock
    UpgradeTier --> ReleaseLock
    
    Client --> SubmitClaim
    SubmitClaim --> ValidateClaim
    ValidateClaim -->|Hợp lệ| CreateTicket
    ValidateClaim -->|Quá 30 ngày hoặc trùng lặp| SubmitClaim
    CreateTicket --> AdminReview
    AdminReview -->|Chấp thuận| CreditPoints
    CreditPoints --> TriggerAccrual
```

---

## 2. Sequence Diagram Clean Architecture: 7 Use Cases & Core Engine

---

### UC-1: Xem Hồ sơ & Thẻ Hội viên Tổng quan
**Endpoint:** `GET /api/v1/loyalty/members/me`  
**Mô tả:** Trả về toàn bộ hồ sơ hội viên, điểm khả dụng, điểm trọn đời, thông tin hạng thẻ hiện tại, tiến độ lên hạng kế tiếp và các bộ đếm hành động nhanh.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng (PWA / Kiosk)
    participant C as MemberLoyaltyController<br>(Presentation)
    participant UC as GetMemberProfileUseCaseImpl<br>(Application Orchestrator)
    participant MPR as MemberProfileJpaRepository<br>(Infrastructure)
    participant TDR as TierDefinitionJpaRepository<br>(Infrastructure)
    participant SCR as SupportClaimJpaRepository<br>(Infrastructure)
    participant UTL as LoyaltyTierUtils<br>(Common Util)
    participant DB as PostgreSQL 16 (DB)

    Client->>C: GET /api/v1/loyalty/members/me<br>[Header: X-Tenant-ID, Bearer Token]
    activate C
    C->>UC: execute()
    activate UC

    Note over UC: Step 1: Lấy userId & tenantId từ SecurityContext & TenantContext
    UC->>MPR: findByTenantIdAndUserId(tenantId, userId)
    activate MPR
    MPR->>DB: SELECT member_profiles WHERE tenant_id = tenantId AND user_id = userId
    DB-->>MPR: MemberProfileJpaEntity
    MPR-->>UC: Optional<MemberProfileJpaEntity>
    deactivate MPR

    Note over UC: Step 2: Lấy danh sách 6 hạng thẻ của tenant
    UC->>TDR: findByTenantIdOrderByTierOrderAsc(tenantId)
    activate TDR
    TDR->>DB: SELECT tier_definitions WHERE tenant_id = tenantId ORDER BY tier_order ASC
    DB-->>TDR: List<TierDefinitionJpaEntity>
    TDR-->>UC: List<TierDefinitionJpaEntity>
    deactivate TDR

    Note over UC: Step 3: Tính toán tiến độ thăng hạng
    UC->>UTL: calculateProgress(currentTierId, lifetimePoints, tiers)
    UTL-->>UC: TierProgressResult (pointsToNextTier, nextTierName, progressPercentage)

    Note over UC: Step 4: Kiểm tra khiếu nại đang xử lý
    UC->>SCR: existsByTenantIdAndMemberIdAndStatus(tenantId, memberId, 'PENDING')
    activate SCR
    SCR->>DB: SELECT count > 0 FROM support_claims WHERE status = 'PENDING'
    DB-->>SCR: boolean hasNotice
    SCR-->>UC: boolean hasNotice
    deactivate SCR

    Note over UC: Step 5: Đóng gói MemberProfileResponse
    UC-->>C: MemberProfileResponse
    deactivate UC
    C-->>Client: 200 OK: ApiResponse<MemberProfileResponse>
    deactivate C
```

---

### UC-2: Cập nhật Thông tin Cá nhân
**Endpoint:** `PUT /api/v1/loyalty/members/me`  
**Mô tả:** Chỉnh sửa họ tên, email, ngày sinh, giới tính của hội viên.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng
    participant C as MemberLoyaltyController<br>(Presentation)
    participant UC as UpdateProfileUseCaseImpl<br>(Application Orchestrator)
    participant UR as UserRepository<br>(Auth Infrastructure)
    participant MPR as MemberProfileJpaRepository<br>(Loyalty Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>C: PUT /api/v1/loyalty/members/me<br>[Body: fullName, email, dateOfBirth, gender]
    activate C
    C->>UC: execute(request)
    activate UC

    Note over UC: Step 1: Kiểm tra tính duy nhất của Email mới
    UC->>UR: existsByTenantIdAndEmailAndIdNot(tenantId, newEmail, userId)
    activate UR
    UR->>DB: SELECT count FROM users WHERE email = newEmail AND id != userId
    DB-->>UR: boolean exists
    UR-->>UC: boolean exists
    deactivate UR

    Note over UC: Step 2: Cập nhật thông tin trong users và member_profiles
    UC->>MPR: findByTenantIdAndUserId(tenantId, userId)
    activate MPR
    MPR->>DB: SELECT * FROM member_profiles WHERE user_id = userId
    DB-->>MPR: MemberProfileJpaEntity
    MPR-->>UC: MemberProfileJpaEntity
    deactivate MPR

    Note over UC: Step 3: Lưu thay đổi thông tin
    UC->>MPR: save(memberProfile)
    activate MPR
    MPR->>DB: UPDATE member_profiles SET date_of_birth, gender, updated_at
    DB-->>MPR: updated
    MPR-->>UC: MemberProfileJpaEntity
    deactivate MPR

    UC-->>C: UpdateProfileResponse
    deactivate UC
    C-->>Client: 200 OK: ApiResponse<UpdateProfileResponse>
    deactivate C
```

---

### UC-3: Hoàn thiện Hồ sơ Sau Xác thực OTP
**Endpoint:** `POST /api/v1/loyalty/members/complete-profile`  
**Mô tả:** Kích hoạt tài khoản và bổ sung họ tên, email sau khi đăng nhập SĐT lần đầu.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng
    participant C as MemberLoyaltyController<br>(Presentation)
    participant UC as CompleteProfileUseCaseImpl<br>(Application Orchestrator)
    participant UR as UserRepository<br>(Auth Infrastructure)
    participant MPR as MemberProfileJpaRepository<br>(Loyalty Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>C: POST /api/v1/loyalty/members/complete-profile<br>[Body: fullName, email, dateOfBirth, gender]
    activate C
    C->>UC: execute(request)
    activate UC

    Note over UC: Step 1: Xác thực thông tin bắt buộc (fullName, email)
    UC->>UR: findByIdAndTenantId(userId, tenantId)
    activate UR
    UR->>DB: SELECT * FROM users WHERE id = userId
    DB-->>UR: UserJpaEntity
    UR-->>UC: UserJpaEntity
    deactivate UR

    Note over UC: Step 2: Cập nhật tên, email và hồ sơ thành viên
    UC->>MPR: findByTenantIdAndUserId(tenantId, userId)
    activate MPR
    MPR->>DB: SELECT * FROM member_profiles WHERE user_id = userId
    DB-->>MPR: MemberProfileJpaEntity
    MPR-->>UC: MemberProfileJpaEntity
    deactivate MPR

    UC->>MPR: save(updatedProfile)
    activate MPR
    MPR->>DB: UPDATE member_profiles SET ...
    DB-->>MPR: updated
    MPR-->>UC: MemberProfileJpaEntity
    deactivate MPR

    UC-->>C: CompleteProfileResponse
    deactivate UC
    C-->>Client: 200 OK: ApiResponse<CompleteProfileResponse>
    deactivate C
```

---

### UC-4: Tải Lên Ảnh Đại Diện Hội Viên
**Endpoint:** `POST /api/v1/loyalty/members/me/avatar`  
**Mô tả:** Khách hàng upload ảnh đại diện (giới hạn $\le 5\text{MB}$, định dạng JPEG, PNG, WebP).

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng
    participant C as MemberLoyaltyController<br>(Presentation)
    participant UC as UploadAvatarUseCaseImpl<br>(Application Orchestrator)
    participant MPR as MemberProfileJpaRepository<br>(Loyalty Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>C: POST /api/v1/loyalty/members/me/avatar<br>[Multipart: file]
    activate C
    C->>UC: execute(file)
    activate UC

    Note over UC: Step 1: Kiểm tra dung lượng (<= 5MB) & định dạng ảnh (JPEG/PNG/WebP)
    Note over UC: Step 2: Lưu trữ ảnh và tạo đường dẫn avatarUrl
    
    Note over UC: Step 3: Cập nhật avatar_url trong member_profiles
    UC->>MPR: findByTenantIdAndUserId(tenantId, userId)
    activate MPR
    MPR->>DB: SELECT * FROM member_profiles WHERE user_id = userId
    DB-->>MPR: MemberProfileJpaEntity
    MPR-->>UC: MemberProfileJpaEntity
    deactivate MPR

    UC->>MPR: save(updatedEntity)
    activate MPR
    MPR->>DB: UPDATE member_profiles SET avatar_url = avatarUrl, updated_at = now()
    DB-->>MPR: updated
    MPR-->>UC: MemberProfileJpaEntity
    deactivate MPR

    UC-->>C: AvatarUploadResponse (avatarUrl, uploadedAt)
    deactivate UC
    C-->>Client: 200 OK: ApiResponse<AvatarUploadResponse>
    deactivate C
```

---

### UC-5: Danh sách 6 Cấp Bậc Thẻ & Đặc Quyền
**Endpoint:** `GET /api/v1/loyalty/tiers`  
**Mô tả:** Lấy danh sách toàn bộ 6 cấp bậc thành viên (New $\rightarrow$ Silver $\rightarrow$ Gold $\rightarrow$ Platinum $\rightarrow$ Diamond $\rightarrow$ Ruby) kèm quyền lợi và mốc điểm.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng / POS
    participant C as MemberLoyaltyController<br>(Presentation)
    participant UC as GetTierDefinitionsUseCaseImpl<br>(Application Orchestrator)
    participant TDR as TierDefinitionJpaRepository<br>(Loyalty Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>C: GET /api/v1/loyalty/tiers<br>[Header: X-Tenant-ID]
    activate C
    C->>UC: execute()
    activate UC

    Note over UC: Step 1: Truy vấn 6 bậc thẻ theo thứ tự thăng tiến
    UC->>TDR: findByTenantIdOrderByTierOrderAsc(tenantId)
    activate TDR
    TDR->>DB: SELECT * FROM tier_definitions WHERE tenant_id = tenantId ORDER BY tier_order ASC
    DB-->>TDR: List<TierDefinitionJpaEntity>
    TDR-->>UC: List<TierDefinitionJpaEntity>
    deactivate TDR

    Note over UC: Step 2: Ánh xạ sang List<TierDefinitionResponse>
    UC-->>C: List<TierDefinitionResponse>
    deactivate UC
    C-->>Client: 200 OK: ApiResponse<List<TierDefinitionResponse>>
    deactivate C
```

---

### UC-6: Lịch Sử Sổ Cái Biến Động Điểm Phân Trang
**Endpoint:** `GET /api/v1/loyalty/members/me/points`  
**Mô tả:** Truy vấn phân trang lịch sử cộng/trừ điểm từ bảng phân vùng `points_ledger` theo thời gian giảm dần.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng
    participant C as MemberLoyaltyController<br>(Presentation)
    participant UC as GetPointsLedgerHistoryUseCaseImpl<br>(Application Orchestrator)
    participant MPR as MemberProfileJpaRepository<br>(Loyalty Infrastructure)
    participant PLR as PointsLedgerJpaRepository<br>(Ledger Infrastructure)
    participant DB as PostgreSQL 16 (DB - Partitioned)

    Client->>C: GET /api/v1/loyalty/members/me/points?page=0&size=20<br>[Header: X-Tenant-ID, Bearer Token]
    activate C
    C->>UC: execute(pageable)
    activate UC

    Note over UC: Step 1: Xác định memberId từ userId
    UC->>MPR: findByTenantIdAndUserId(tenantId, userId)
    activate MPR
    MPR->>DB: SELECT id FROM member_profiles WHERE user_id = userId
    DB-->>MPR: MemberProfileJpaEntity
    MPR-->>UC: MemberProfileJpaEntity
    deactivate MPR

    Note over UC: Step 2: Truy vấn phân trang trên bảng phân vùng points_ledger
    UC->>PLR: findByTenantIdAndMemberIdOrderByCreatedAtDesc(tenantId, memberId, pageable)
    activate PLR
    PLR->>DB: SELECT * FROM points_ledger WHERE tenant_id = tenantId AND member_id = memberId ORDER BY created_at DESC LIMIT 20 OFFSET 0
    DB-->>PLR: Page<PointsLedgerJpaEntity>
    PLR-->>UC: Page<PointsLedgerJpaEntity>
    deactivate PLR

    Note over UC: Step 3: Đóng gói PageResponse<PointsLedgerEntryResponse>
    UC-->>C: PageResponse<PointsLedgerEntryResponse>
    deactivate UC
    C-->>Client: 200 OK: ApiResponse<PageResponse<PointsLedgerEntryResponse>>
    deactivate C
```

---

### UC-7: Gửi Khiếu Nại Thiếu Điểm Hóa Đơn
**Endpoint:** `POST /api/v1/loyalty/claims`  
**Mô tả:** Khách hàng gửi yêu cầu cộng bù điểm cho hóa đơn tại quầy trong vòng 30 ngày kèm mã hóa đơn và ảnh chụp biên lai.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    actor Client as Khách hàng
    participant C as SupportClaimController<br>(Presentation)
    participant UC as SubmitSupportClaimUseCaseImpl<br>(Application Orchestrator)
    participant MPR as MemberProfileJpaRepository<br>(Loyalty Infrastructure)
    participant SCR as SupportClaimJpaRepository<br>(Loyalty Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Client->>C: POST /api/v1/loyalty/claims<br>[Body: receiptCode, orderDate, billAmount, storeId, billPhotoUrl]
    activate C
    C->>UC: execute(request)
    activate UC

    Note over UC: Step 1: Kiểm tra thời hạn hóa đơn (không quá 30 ngày)
    Note over UC: Step 2: Kiểm tra trùng lặp mã hóa đơn trong tenant
    UC->>SCR: existsByTenantIdAndReceiptCode(tenantId, receiptCode)
    activate SCR
    SCR->>DB: SELECT count FROM support_claims WHERE tenant_id = tenantId AND receipt_code = receiptCode
    DB-->>SCR: boolean exists
    SCR-->>UC: boolean exists
    deactivate SCR

    Note over UC: Step 3: Lấy thông tin memberId
    UC->>MPR: findByTenantIdAndUserId(tenantId, userId)
    activate MPR
    MPR-->>UC: MemberProfileJpaEntity
    deactivate MPR

    Note over UC: Step 4: Tạo phiếu khiếu nại mới trạng thái PENDING
    UC->>SCR: save(newSupportClaim)
    activate SCR
    SCR->>DB: INSERT INTO support_claims (tenant_id, member_id, receipt_code, order_date, bill_amount, status...) VALUES (...)
    DB-->>SCR: saved entity
    SCR-->>UC: SupportClaimJpaEntity
    deactivate SCR

    UC-->>C: SupportClaimResponse
    deactivate UC
    C-->>Client: 201 Created: ApiResponse<SupportClaimResponse>
    deactivate C
```

---

### Core Engine: Động Cơ Tích Điểm, Khóa Phân Tán Redisson & Thăng Hạng Thẻ (AccruePointsUseCase)
**Mô tả:** Động cơ lõi nội bộ phục vụ cộng/trừ điểm từ mọi nguồn (`ORDER_EARN`, `VOUCHER_REDEEM`, `GIFT_REDEEM`, `CLAIM_CREDIT`, `KIOSK_EARN`).

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1e293b', 'primaryTextColor': '#f8fafc', 'primaryBorderColor': '#38bdf8', 'lineColor': '#38bdf8', 'signalColor': '#f5b800'}}}%%
sequenceDiagram
    autonumber
    participant Caller as Caller (Order / Reward / Claim / Kiosk)
    participant UC as AccruePointsUseCaseImpl<br>(Application Engine)
    participant Redisson as Redisson Lock (Redis 7)
    participant MPR as MemberProfileJpaRepository<br>(Loyalty Infrastructure)
    participant TDR as TierDefinitionJpaRepository<br>(Loyalty Infrastructure)
    participant PLR as PointsLedgerJpaRepository<br>(Ledger Infrastructure)
    participant DB as PostgreSQL 16 (DB)

    Caller->>UC: execute(command)
    activate UC

    Note over UC: Step 1: Chiếm giữ Redisson Distributed Lock
    UC->>Redisson: getLock("lock:tenant:" + tenantId + ":member:" + memberId).tryLock(3s, 5s)
    activate Redisson
    Redisson-->>UC: lockAcquired == true
    deactivate Redisson

    critical Trong Giao Tác ACID (@Transactional)
        Note over UC: Step 2: Đọc hồ sơ hội viên
        UC->>MPR: findByIdAndTenantId(memberId, tenantId)
        activate MPR
        MPR->>DB: SELECT * FROM member_profiles WHERE id = memberId
        DB-->>MPR: MemberProfileJpaEntity
        MPR-->>UC: MemberProfileJpaEntity
        deactivate MPR

        Note over UC: Step 3: Kiểm tra số dư khả dụng (nếu pointsDelta < 0)
        Note over UC: Step 4: Tính toán số dư mới: newCurrentPoints, newLifetimePoints

        Note over UC: Step 5: Tự động xét mốc thăng hạng thẻ
        UC->>TDR: findByTenantIdOrderByTierOrderDesc(tenantId)
        activate TDR
        TDR->>DB: SELECT * FROM tier_definitions WHERE tenant_id = tenantId ORDER BY tier_order DESC
        DB-->>TDR: List<TierDefinitionJpaEntity>
        TDR-->>UC: List<TierDefinitionJpaEntity>
        deactivate TDR

        Note over UC: Tìm bậc cao nhất có min_points_required <= newLifetimePoints
        Note over UC: Cập nhật current_tier_id nếu có bước thăng hạng

        Note over UC: Step 6: INSERT bản ghi mới vào points_ledger (Bất biến)
        UC->>PLR: save(newPointsLedgerEntry)
        activate PLR
        PLR->>DB: INSERT INTO points_ledger (id, tenant_id, member_id, points_delta, balance_after, created_at...) VALUES (...)
        DB-->>PLR: saved
        PLR-->>UC: PointsLedgerJpaEntity
        deactivate PLR

        Note over UC: Step 7: Cập nhật số dư cache trên member_profiles
        UC->>MPR: save(memberProfile)
        activate MPR
        MPR->>DB: UPDATE member_profiles SET current_points = newCurrent, lifetime_points = newLifetime, current_tier_id = newTier
        DB-->>MPR: updated
        MPR-->>UC: MemberProfileJpaEntity
        deactivate MPR
    end

    Note over UC: Step 8: Giải phóng Redisson Distributed Lock
    UC->>Redisson: unlock()
    activate Redisson
    Redisson-->>UC: lock released
    deactivate Redisson

    UC-->>Caller: PointsLedgerJpaEntity
    deactivate UC
```

