# Tài Liệu Luồng Nghiệp Vụ & Kiến Trúc Phân Hệ Hộp Thư Thông Báo & Hỗ Trợ Khách Hàng (Business Flows & Architecture)

> **Tài liệu đặc tả luồng nghiệp vụ và tương tác 4 lớp Clean Architecture**  
> **Ánh xạ Use Cases:** 8.1 Đếm chưa đọc, 8.2 Danh sách đa danh mục, 8.3 Chi tiết thông báo, 8.4 Đánh dấu 1 tin đã đọc, 8.5 Đánh dấu tất cả đã đọc, 8.6 Gửi phiếu hỗ trợ thiếu điểm  
> **Module Backend:** `com.ordera.modules.notification`  

---

## 1. Sơ Đồ Tổng Quan Luồng Nghiệp Vụ Hộp Thư & Hỗ Trợ (Notification & Support Flowchart)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1E293B', 'primaryTextColor': '#F8FAFC', 'primaryBorderColor': '#38BDF8', 'lineColor': '#94A3B8', 'secondaryColor': '#0F172A', 'tertiaryColor': '#1E293B' }}}%%
flowchart TD
    START(["Khách Hàng Mở Ứng Dụng Ordera Loyalty"])

    subgraph FLOW_UNREAD ["8.1 Đếm Số Lượng Thông Báo Chưa Đọc (Unread Badge)"]
        CHECK_UNREAD["Client gọi GET /api/v1/notifications/unread-count"]
        COUNT_ACTIVE["Lấy tổng số system_notifications đang active & chưa hết hạn trong tenant"]
        COUNT_READ["Đếm số thông báo user_notifications đã đọc (is_read = true)"]
        CALC_DIFF["unreadCount = max(0, activeCount - readCount)"]
        RENDER_BADGE["Render badge đỏ trên icon quả chuông Navbar"]
    end

    subgraph FLOW_LIST ["8.2 Danh Sách Thông Báo Đa Danh Mục (Fan-out on Read)"]
        SELECT_TAB["Khách chọn Tab Messages (Hỗ trợ) hoặc News (Tin tức)"]
        FETCH_PAGE["Lấy system_notifications phân trang (size=20, page=0)"]
        MERGE_READ["Ánh xạ trạng thái isRead từ user_notifications"]
        RENDER_LIST["Render danh sách thông báo kèm banner & nhãn thao tác"]
    end

    subgraph FLOW_DETAIL ["8.3 Xem Chi Tiết Thông Báo & Tự Động Đánh Dấu Đã Đọc"]
        CLICK_ITEM["Khách bấm vào một thông báo cụ thể"]
        LOAD_DETAIL["Truy vấn thông tin chi tiết bài viết (banner, content, hotline, email)"]
        AUTO_MARK["Tự động cập nhật user_notifications: is_read = true, read_at = now()"]
        SHOW_MODAL["Hiển thị Modal chi tiết 05.03"]
    end

    subgraph FLOW_MANUAL_READ ["8.4 & 8.5 Đánh Dấu Đã Đọc Thủ Công"]
        MARK_ONE["PATCH /api/v1/notifications/{id}/read"]
        MARK_ALL["PUT /api/v1/notifications/read-all"]
        UPDATE_STATUS["Cập nhật user_notifications sang is_read = true"]
        RESET_BADGE["Cập nhật badge unreadCount về 0"]
    end

    subgraph FLOW_INQUIRY ["8.6 Gửi Phiếu Khiếu Nại Thất Lạc Điểm Cũ (Modal 05.03)"]
        CLICK_CLAIM["Khách bấm nút [Submit Form] tại bài viết Missing Your Points?"]
        INPUT_FORM["Khách điền Email, SĐT cũ, Mã biên lai, Ghi chú và Ảnh chứng từ"]
        VALIDATE_FORM{"Dữ liệu hợp lệ?<br/>(Email, Phone, Note <= 500 ký tự)"}
        GEN_TICKET["Sinh mã phiếu INQ-YYYYMMDD-XXX"]
        SAVE_INQUIRY[("Lưu vào bảng support_inquiries<br/>Status: PENDING")]
        ADMIN_RESOLVE["CSKH đối soát hệ thống cũ & phản hồi trong 24 giờ"]
    end

    START --> CHECK_UNREAD
    CHECK_UNREAD --> COUNT_ACTIVE
    COUNT_ACTIVE --> COUNT_READ
    COUNT_READ --> CALC_DIFF
    CALC_DIFF --> RENDER_BADGE

    RENDER_BADGE --> SELECT_TAB
    SELECT_TAB --> FETCH_PAGE
    FETCH_PAGE --> MERGE_READ
    MERGE_READ --> RENDER_LIST

    RENDER_LIST --> CLICK_ITEM
    CLICK_ITEM --> LOAD_DETAIL
    LOAD_DETAIL --> AUTO_MARK
    AUTO_MARK --> SHOW_MODAL

    RENDER_LIST --> MARK_ALL
    MARK_ALL --> UPDATE_STATUS
    UPDATE_STATUS --> RESET_BADGE

    SHOW_MODAL --> CLICK_CLAIM
    CLICK_CLAIM --> INPUT_FORM
    INPUT_FORM --> VALIDATE_FORM
    VALIDATE_FORM -- "Hợp lệ" --> GEN_TICKET
    GEN_TICKET --> SAVE_INQUIRY
    SAVE_INQUIRY --> ADMIN_RESOLVE
    VALIDATE_FORM -- "Không hợp lệ" --> INPUT_FORM
```

---

## 2. Sequence Diagrams 4 Lớp Clean Architecture (6 Use Cases)

### 2.1. Use Case 8.1: Lấy Số Lượng Thông Báo Chưa Đọc (`GetUnreadNotificationCountUseCase`)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1E293B', 'primaryTextColor': '#F8FAFC', 'primaryBorderColor': '#38BDF8', 'lineColor': '#94A3B8', 'secondaryColor': '#0F172A', 'tertiaryColor': '#1E293B' }}}%%
sequenceDiagram
    autonumber
    actor User as Khách Hàng (Hội Viên)
    participant UI as Navbar / Bell Icon
    participant Controller as NotificationController
    participant UseCase as GetUnreadNotificationCountUseCaseImpl
    participant SysRepo as SystemNotificationJpaRepository
    participant UserNotifRepo as UserNotificationJpaRepository

    User->>UI: Mở ứng dụng Ordera
    UI->>Controller: GET /api/v1/notifications/unread-count<br/>Headers: X-Tenant-ID, Authorization
    Controller->>UseCase: execute(tenantId, userId)
    
    UseCase->>SysRepo: countActiveNotifications(tenantId, now)
    SysRepo-->>UseCase: activeTotalCount (ví dụ: 5)
    
    UseCase->>UserNotifRepo: countReadNotifications(tenantId, userId)
    UserNotifRepo-->>UseCase: readCount (ví dụ: 4)
    
    UseCase->>UseCase: unreadCount = max(0, activeTotalCount - readCount)
    UseCase-->>Controller: UnreadCountResponse(unreadCount=1)
    Controller-->>UI: 200 OK (ApiResponse.ok(MSG_UNREAD_COUNT_RETRIEVED, data))
    UI-->>User: Hiển thị chấm đỏ badge (1) trên icon quả chuông
```

---

### 2.2. Use Case 8.2: Danh Sách Thông Báo Đa Danh Mục Phân Trang (`GetNotificationsListUseCase`)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1E293B', 'primaryTextColor': '#F8FAFC', 'primaryBorderColor': '#38BDF8', 'lineColor': '#94A3B8', 'secondaryColor': '#0F172A', 'tertiaryColor': '#1E293B' }}}%%
sequenceDiagram
    autonumber
    actor User as Khách Hàng (Hội Viên)
    participant UI as NotificationListView (Figma 05.02)
    participant Controller as NotificationController
    participant UseCase as GetNotificationsListUseCaseImpl
    participant SysRepo as SystemNotificationJpaRepository
    participant UserNotifRepo as UserNotificationJpaRepository
    participant Mapper as NotificationDtoMapper

    User->>UI: Bấm chọn Tab "Messages" hoặc "News"
    UI->>Controller: GET /api/v1/notifications?type=MESSAGES&page=0&size=20<br/>Headers: X-Tenant-ID, Authorization
    Controller->>UseCase: execute(tenantId, userId, "MESSAGES", isRead, 0, 20)
    
    UseCase->>UseCase: normalizeCategory("MESSAGES") & validatePagination()
    UseCase->>SysRepo: findByTenantIdAndCategory(tenantId, category, pageable)
    SysRepo-->>UseCase: Page<SystemNotificationJpaEntity>
    
    UseCase->>UserNotifRepo: findReadNotificationIds(tenantId, userId)
    UserNotifRepo-->>UseCase: Set<UUID> readNotificationIds
    
    UseCase->>Mapper: toSummaryDtoList(entities, readNotificationIds)
    Mapper-->>UseCase: List<NotificationSummaryResponse>
    
    UseCase-->>Controller: PageResponse<NotificationSummaryResponse>
    Controller-->>UI: 200 OK (ApiResponse.ok(MSG_NOTIFICATIONS_LIST_RETRIEVED, pageData))
    UI-->>User: Hiển thị danh sách card bài viết, gắn tag Chưa đọc/Đã đọc
```

---

### 2.3. Use Case 8.3: Chi Tiết Thông Báo & Tự Động Đánh Dấu Đã Đọc (`GetNotificationDetailUseCase`)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1E293B', 'primaryTextColor': '#F8FAFC', 'primaryBorderColor': '#38BDF8', 'lineColor': '#94A3B8', 'secondaryColor': '#0F172A', 'tertiaryColor': '#1E293B' }}}%%
sequenceDiagram
    autonumber
    actor User as Khách Hàng (Hội Viên)
    participant UI as NotificationDetailModal (Figma 05.03)
    participant Controller as NotificationController
    participant UseCase as GetNotificationDetailUseCaseImpl
    participant SysRepo as SystemNotificationJpaRepository
    participant UserNotifRepo as UserNotificationJpaRepository
    participant Mapper as NotificationDtoMapper

    User->>UI: Bấm chọn xem bài viết thông báo
    UI->>Controller: GET /api/v1/notifications/{id}<br/>Headers: X-Tenant-ID, Authorization
    Controller->>UseCase: execute(tenantId, userId, notificationId)
    
    UseCase->>SysRepo: findByIdAndTenantId(notificationId, tenantId)
    alt Không tìm thấy bài viết
        SysRepo-->>UseCase: Optional.empty()
        UseCase-->>Controller: ném BusinessException(NOTIFICATION_NOT_FOUND)
    else Tìm thấy bài viết
        SysRepo-->>UseCase: SystemNotificationJpaEntity
        
        UseCase->>UserNotifRepo: findByTenantIdAndUserIdAndNotificationId(tenantId, userId, notificationId)
        alt Bản ghi đọc tin chưa tồn tại
            UseCase->>UserNotifRepo: save(new UserNotification(isRead=true, readAt=now()))
        else Đã tồn tại nhưng is_read = false
            UseCase->>UserNotifRepo: updateReadStatus(isRead=true, readAt=now())
        end
        
        UseCase->>Mapper: toDetailDto(entity, isRead=true)
        Mapper-->>UseCase: NotificationDetailResponse
        UseCase-->>Controller: NotificationDetailResponse
    end

    Controller-->>UI: 200 OK (ApiResponse.ok(MSG_NOTIFICATION_DETAIL_RETRIEVED, data))
    UI-->>User: Hiển thị banner lớn, toàn văn nội dung & Hotline/Email liên hệ
```

---

### 2.4. Use Case 8.4: Đánh Dấu Một Thông Báo Đã Đọc (`MarkNotificationReadUseCase`)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1E293B', 'primaryTextColor': '#F8FAFC', 'primaryBorderColor': '#38BDF8', 'lineColor': '#94A3B8', 'secondaryColor': '#0F172A', 'tertiaryColor': '#1E293B' }}}%%
sequenceDiagram
    autonumber
    actor User as Khách Hàng (Hội Viên)
    participant UI as Hộp Thư / Nút Đánh Dấu
    participant Controller as NotificationController
    participant UseCase as MarkNotificationReadUseCaseImpl
    participant SysRepo as SystemNotificationJpaRepository
    participant UserNotifRepo as UserNotificationJpaRepository

    User->>UI: Quẹt hoặc bấm đánh dấu đã đọc
    UI->>Controller: PATCH /api/v1/notifications/{id}/read<br/>Headers: X-Tenant-ID, Authorization
    Controller->>UseCase: execute(tenantId, userId, notificationId)
    
    UseCase->>SysRepo: existsByIdAndTenantId(notificationId, tenantId)
    alt Thông báo không tồn tại
        SysRepo-->>UseCase: false
        UseCase-->>Controller: ném BusinessException(NOTIFICATION_NOT_FOUND)
    else Tồn tại
        SysRepo-->>UseCase: true
        UseCase->>UserNotifRepo: findByTenantIdAndUserIdAndNotificationId(tenantId, userId, notificationId)
        UseCase->>UserNotifRepo: saveOrUpdateReadRecord(tenantId, userId, notificationId, now)
        UserNotifRepo-->>UseCase: UserNotificationJpaEntity
        UseCase-->>Controller: MarkReadResponse(id, isRead=true, readAt=now)
    end

    Controller-->>UI: 200 OK (ApiResponse.ok(MSG_NOTIFICATION_MARKED_READ, data))
    UI-->>User: Gỡ chấm xanh chưa đọc trên giao diện
```

---

### 2.5. Use Case 8.5: Đánh Dấu Tất Cả Thông Báo Đã Đọc (`MarkAllNotificationsReadUseCase`)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1E293B', 'primaryTextColor': '#F8FAFC', 'primaryBorderColor': '#38BDF8', 'lineColor': '#94A3B8', 'secondaryColor': '#0F172A', 'tertiaryColor': '#1E293B' }}}%%
sequenceDiagram
    autonumber
    actor User as Khách Hàng (Hội Viên)
    participant UI as Nút "Read all" (Figma 05.02)
    participant Controller as NotificationController
    participant UseCase as MarkAllNotificationsReadUseCaseImpl
    participant SysRepo as SystemNotificationJpaRepository
    participant UserNotifRepo as UserNotificationJpaRepository

    User->>UI: Nhấn nút "Read all" góc trên bên phải
    UI->>Controller: PUT /api/v1/notifications/read-all<br/>Headers: X-Tenant-ID, Authorization
    Controller->>UseCase: execute(tenantId, userId)
    
    UseCase->>SysRepo: findActiveNotificationIds(tenantId, now)
    SysRepo-->>UseCase: List<UUID> allActiveIds
    
    UseCase->>UserNotifRepo: findReadNotificationIds(tenantId, userId)
    UserNotifRepo-->>UseCase: Set<UUID> alreadyReadIds
    
    UseCase->>UseCase: filter unreadIds = allActiveIds - alreadyReadIds
    UseCase->>UserNotifRepo: markAllAsRead(tenantId, userId, unreadIds, now)
    UserNotifRepo-->>UseCase: số lượng đã cập nhật (markedCount)
    
    UseCase-->>Controller: MarkAllReadResponse(markedCount, unreadCount=0)
    Controller-->>UI: 200 OK (ApiResponse.ok(MSG_ALL_NOTIFICATIONS_MARKED_READ, data))
    UI-->>User: Xóa toàn bộ huy hiệu đỏ & đánh dấu xám tất cả tin
```

---

### 2.6. Use Case 8.6: Tiếp Nhận Phiếu Khiếu Nại Thiếu Điểm Cũ (`SubmitMissingPointsInquiryUseCase`)

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1E293B', 'primaryTextColor': '#F8FAFC', 'primaryBorderColor': '#38BDF8', 'lineColor': '#94A3B8', 'secondaryColor': '#0F172A', 'tertiaryColor': '#1E293B' }}}%%
sequenceDiagram
    autonumber
    actor Customer as Khách Hàng (Hội Viên hoặc Khách Vãng Lai)
    participant UI as MissingPointsModal (05.03)
    participant Controller as SupportInquiryController
    participant UseCase as SubmitMissingPointsInquiryUseCaseImpl
    participant InquiryRepo as SupportInquiryJpaRepository
    participant Mapper as NotificationDtoMapper

    Customer->>UI: Điền Form đối soát và bấm [Submit Form]
    UI->>Controller: POST /api/v1/support/missing-points/submit<br/>Headers: X-Tenant-ID, Authorization (Optional)
    Controller->>UseCase: execute(tenantId, userId, request)
    
    UseCase->>UseCase: validateInquiryInput(request)
    Note over UseCase: Kiểm tra Email hợp lệ, SĐT E.164,<br/>Note 5-500 ký tự, Tiền >= 0.00
    
    UseCase->>UseCase: generateTicketCode(tenantId, today)
    UseCase->>InquiryRepo: countTodayInquiries(tenantId, today)
    InquiryRepo-->>UseCase: seqNumber (ví dụ: 8)
    UseCase->>UseCase: ticketCode = "INQ-20261109-009"
    
    UseCase->>Mapper: toJpaEntity(tenantId, userId, ticketCode, request)
    Mapper-->>UseCase: SupportInquiryJpaEntity(status=PENDING)
    
    UseCase->>InquiryRepo: save(entity)
    InquiryRepo-->>UseCase: Saved Entity
    
    UseCase-->>Controller: SubmitInquiryResponse(ticketId="INQ-20261109-009", status="PENDING", ...)
    Controller-->>UI: 201 Created (ApiResponse.created(MSG_INQUIRY_SUBMITTED, data))
    UI-->>Customer: Hiển thị thông báo gửi thành công & mã đối soát INQ
```

