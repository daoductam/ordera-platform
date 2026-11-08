# Sổ Tay Luồng Nghiệp Vụ & Biểu Đồ Trình Tự: Phân Hệ Auth & Identity
## Business & Sequence Flows Guide (Clean Architecture & Dark Mode Mermaid)

> **Dành cho:** Kỹ sư phần mềm, Kiến trúc sư giải pháp, QA và thành viên dự án Ordera.  
> **Mục tiêu:** Cung cấp tài liệu trực quan hóa toàn diện về luồng nghiệp vụ xác thực OTP không mật khẩu, đăng ký thành viên 2 bước, cấp phát và xoay vòng JWT HMAC-SHA512, quản lý Cooldown 120s, ngưỡng khóa 5 lần nhập sai, và thu hồi token vào Redis Blacklist theo Clean Architecture 4 lớp.

---

## 📑 Mục Lục
1. [Mô hình Luồng 4 Lớp Clean Architecture & Bảng Mã Lỗi (Quick Reference)](#1-mô-hình-luồng-4-lớp-clean-architecture--bảng-mã-lỗi)
2. [Use Case 1: Gửi Mã OTP Đăng Nhập (`SendOtpUseCase`)](#use-case-1-gửi-mã-otp-đăng-nhập-sendotpusecase)
3. [Use Case 2: Xác Thực Mã OTP Đăng Nhập (`VerifyOtpUseCase`)](#use-case-2-xác-thực-mã-otp-đăng-nhập-verifyotpusecase)
4. [Use Case 3: Đăng Ký Hội Viên & Kích Hoạt Thẻ New Member (`RegisterMemberUseCase`)](#use-case-3-đăng-ký-hội-viên--kích-hoạt-thẻ-new-member-registermemberusecase)
5. [Use Case 4: Cấp Lại Token - Xoay Vòng Refresh Token (`RefreshTokenUseCase`)](#use-case-4-cấp-lại-token---xoay-vòng-refresh-token-refreshtokenusecase)
6. [Use Case 5: Đăng Xuất & Thu Hồi Token Vào Redis Blacklist (`LogoutUseCase`)](#use-case-5-đăng-xuất--thu-hồi-token-vào-redis-blacklist-logoutusecase)

---

## 1. Mô hình Luồng 4 Lớp Clean Architecture & Bảng Mã Lỗi

### Kiến trúc Phân Tầng Clean Architecture
```mermaid
flowchart LR
    Client(["Client (PWA / Kiosk)"]) -->|HTTP Request| Pre["Presentation: AuthController & Validation"]
    Pre -->|Invoke Command| App["Application: Use Cases & Orchestrators"]
    App -->|Pure Logic| Dom["Domain: User, OtpSession & AuthErrorCode"]
    App -->|Pure Util| Util["Common Util: OtpUtils, SecurityUtils"]
    App -->|Outbound Ports| Port["Application Ports: Storage, SMS, Email, Blacklist"]
    Port -->|Adapters| Infra["Infrastructure: Redis, Telnyx, MailHog, JPA"]
    Infra -->|Data Persistence| Storage[("PostgreSQL & Redis 7")]

    classDef c1 fill:#0c4a6e,stroke:#38bdf8,stroke-width:2px,color:#f0f9ff;
    classDef c2 fill:#451a03,stroke:#f59e0b,stroke-width:2px,color:#fef3c7;
    classDef c3 fill:#064e3b,stroke:#34d399,stroke-width:2px,color:#ecfdf5;
    class Pre,App c1;
    class Dom,Util,Port c2;
    class Infra,Storage c3;
```

### Bảng Mã Lỗi Chuẩn (`AuthErrorCode`)
| HTTP Status | ErrorCode Enum | Ý nghĩa nghiệp vụ |
| :---: | :--- | :--- |
| `400` | `TENANT_HEADER_MISSING` | Thiếu hoặc không hợp lệ header `X-Tenant-ID`. |
| `400` | `OTP_INVALID` | Mã OTP không chính xác, kèm số lần thử còn lại. |
| `400` | `OTP_EXPIRED` | Mã OTP đã quá thời hạn 120 giây. |
| `401` | `INVALID_CREDENTIALS` | Thông tin đăng nhập không hợp lệ. |
| `401` | `TOKEN_EXPIRED` | Access Token đã hết hạn. |
| `401` | `TOKEN_REVOKED` | Token nằm trong Redis Blacklist (đã đăng xuất). |
| `401` | `REFRESH_TOKEN_EXPIRED` | Refresh Token đã quá hạn, yêu cầu xác thực lại. |
| `404` | `USER_NOT_FOUND` | Không tìm thấy tài khoản người dùng theo định danh. |
| `409` | `PHONE_ALREADY_EXISTS` | Số điện thoại đã được đăng ký trong thương hiệu. |
| `409` | `USER_ALREADY_EXISTS` | Số điện thoại hoặc Email đã được đăng ký. |
| `422` | `VALIDATION_FAILED` | Dữ liệu đầu vào sai định dạng (ví dụ: số điện thoại không chuẩn E.164). |
| `429` | `OTP_COOLDOWN_ACTIVE` | Yêu cầu gửi lại khi chưa hết 120s cooldown. |
| `429` | `OTP_MAX_ATTEMPTS_EXCEEDED`| Nhập sai quá 5 lần, phiên bị khóa 15 phút. |

---

## Use Case 1: Gửi Mã OTP Đăng Nhập (`SendOtpUseCase`)

- **API:** `POST /api/v1/auth/otp/send`
- **Mục tiêu:** Kiểm tra Cooldown 120s, sinh OTP 6 số, băm SHA-256 lưu DB audit log, gửi SMS viễn thông.

### 1. Biểu đồ Luồng Nghiệp Vụ (Flowchart)
```mermaid
flowchart TD
    Start(["Client gửi POST /api/v1/auth/otp/send"]) --> CheckTenant{"X-Tenant-ID có tồn tại?"}
    CheckTenant -- Không --> ErrTenant["❌ 400 Bad Request<br>TENANT_HEADER_MISSING"]
    CheckTenant -- Có --> CheckCooldown{"Redis có key cooldown 120s?"}
    CheckCooldown -- Có --> ErrCooldown["❌ 429 Too Many Requests<br>OTP_COOLDOWN_ACTIVE"]
    CheckCooldown -- Không --> GenOtp["OtpUtils sinh 6 số ngẫu nhiên & băm SHA-256"]
    GenOtp --> SaveRedis["Lưu OTP vào Redis TTL 120s & tạo flag Cooldown 120s"]
    SaveRedis --> SendSms["TelnyxSmsAdapter gửi SMS tới SĐT quốc tế"]
    SendSms --> AuditDB["Ghi nhật ký bảng otp_audit_logs"]
    AuditDB --> Success(["✅ 200 OK<br>Trả về maskedPhoneNumber & cooldownSeconds"])

    classDef ok fill:#064e3b,stroke:#10b981,stroke-width:2px,color:#ecfdf5;
    classDef fail fill:#450a0a,stroke:#ef4444,stroke-width:2px,color:#fef2f2;
    class Success ok;
    class ErrTenant,ErrCooldown fail;
```

### 2. Biểu đồ Trình Tự Clean Architecture (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as AuthController
    participant UC as SendOtpUseCase
    participant PortS as OtpStoragePort
    participant PortSMS as SmsSenderPort
    participant AuditRepo as OtpAuditLogRepository
    participant Redis as Redis 7
    participant DB as PostgreSQL

    C->>Ctrl: POST /api/v1/auth/otp/send (X-Tenant-ID, phone)
    Ctrl->>UC: execute(SendOtpRequest)
    UC->>UC: Kiểm tra TenantContext.getTenantId()
    UC->>PortS: isCooldownActive(tenantId, phone)
    PortS->>Redis: EXISTS auth:otp:cooldown:{tenantId}:{phone}
    Redis-->>PortS: true / false
    alt Cooldown đang kích hoạt
        PortS-->>UC: true
        UC-->>Ctrl: throw BusinessException(OTP_COOLDOWN_ACTIVE)
        Ctrl-->>C: 429 Too Many Requests
    end
    UC->>UC: OtpUtils.generateNumericOtp(6)
    UC->>PortS: saveOtp(tenantId, phone, otpCode, 120s)
    PortS->>Redis: SET auth:otp:{tenantId}:{phone} = otpCode (TTL 120s)
    PortS->>Redis: SET auth:otp:cooldown:{tenantId}:{phone} = active (TTL 120s)
    UC->>PortSMS: sendSms(phone, otpCode)
    UC->>AuditRepo: save(OtpAuditLogJpaEntity)
    AuditRepo->>DB: INSERT INTO otp_audit_logs (otp_hash, expires_at...)
    UC-->>Ctrl: SendOtpResponse (maskedPhone, cooldown 120s)
    Ctrl-->>C: 200 OK (ApiResponse)
```

---

## Use Case 2: Xác Thực Mã OTP Đăng Nhập (`VerifyOtpUseCase`)

- **API:** `POST /api/v1/auth/otp/verify`
- **Mục tiêu:** Kiểm tra số lần nhập sai, đối soát mã OTP, cấp phát JWT nếu đã có profile, hoặc trả về `isProfileComplete = false`.

### 1. Biểu đồ Luồng Nghiệp Vụ (Flowchart)
```mermaid
flowchart TD
    Start(["Client gửi POST /api/v1/auth/otp/verify"]) --> CheckAttempts{"Attempts >= 5?"}
    CheckAttempts -- Có --> ErrMax["❌ 429 Too Many Requests<br>OTP_MAX_ATTEMPTS_EXCEEDED"]
    CheckAttempts -- Không --> ReadOtp{"Lấy OTP từ Redis"}
    ReadOtp -- Null/Hết hạn --> ErrExpired["❌ 400 Bad Request<br>OTP_EXPIRED"]
    ReadOtp -- Tồn tại --> MatchOtp{"Mã nhập có khớp không?"}
    MatchOtp -- Sai --> IncAttempt["Tăng bộ đếm attempts trong Redis"]
    IncAttempt --> ErrInvalid["❌ 400 Bad Request<br>OTP_INVALID (còn lại N lần thử)"]
    MatchOtp -- Đúng --> ClearOtp["Xóa OTP & attempts trong Redis"]
    ClearOtp --> FindUser{"Tìm User theo Tenant & SĐT"}
    FindUser -- Chưa có --> NeedProfile(["✅ 200 OK<br>isProfileComplete = false"])
    FindUser -- Đã có --> GenToken["JwtTokenProvider phát sinh Access & Refresh Token"]
    GenToken --> Complete(["✅ 200 OK<br>isProfileComplete = true + TokenPair + User Info"])

    classDef ok fill:#064e3b,stroke:#10b981,stroke-width:2px,color:#ecfdf5;
    classDef warn fill:#78350f,stroke:#f59e0b,stroke-width:2px,color:#fef3c7;
    classDef fail fill:#450a0a,stroke:#ef4444,stroke-width:2px,color:#fef2f2;
    class Complete ok;
    class NeedProfile warn;
    class ErrMax,ErrExpired,ErrInvalid fail;
```

### 2. Biểu đồ Trình Tự Clean Architecture (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as AuthController
    participant UC as VerifyOtpUseCase
    participant PortS as OtpStoragePort
    participant UserRepo as UserRepository
    participant Jwt as JwtTokenProviderService
    participant Redis as Redis 7
    participant DB as PostgreSQL

    C->>Ctrl: POST /api/v1/auth/otp/verify (phone, otpCode)
    Ctrl->>UC: execute(VerifyOtpRequest)
    UC->>PortS: getAttempts(tenantId, phone)
    alt Attempts >= 5
        UC-->>Ctrl: throw BusinessException(OTP_MAX_ATTEMPTS_EXCEEDED)
        Ctrl-->>C: 429 Too Many Requests
    end
    UC->>PortS: getOtp(tenantId, phone)
    alt OTP không tồn tại
        UC-->>Ctrl: throw BusinessException(OTP_EXPIRED)
        Ctrl-->>C: 400 Bad Request
    end
    alt OTP không trùng khớp
        UC->>PortS: incrementAttempts(tenantId, phone)
        UC-->>Ctrl: throw BusinessException(OTP_INVALID)
        Ctrl-->>C: 400 Bad Request
    end
    UC->>PortS: clearOtp(tenantId, phone)
    UC->>UserRepo: findByTenantIdAndPhoneNumber(tenantId, phone)
    UserRepo->>DB: SELECT * FROM users WHERE tenant_id = ? AND phone_number = ?
    alt User chưa tồn tại
        UC-->>Ctrl: VerifyOtpResponse(isProfileComplete = false)
        Ctrl-->>C: 200 OK (Chuyển sang hoàn thiện hồ sơ)
    else User đã tồn tại
        UC->>Jwt: generateTokenPair(user)
        UC-->>Ctrl: VerifyOtpResponse(isProfileComplete = true, tokens, user)
        Ctrl-->>C: 200 OK (Đăng nhập thành công)
    end
```

---

## Use Case 3: Đăng Ký Hội Viên & Kích Hoạt Thẻ New Member (`RegisterMemberUseCase`)

- **API:**
  - `POST /api/v1/auth/register/otp/send` (Gửi OTP qua SMS hoặc Email)
  - `POST /api/v1/auth/register/otp/verify` (Xác thực OTP & tạo User + MemberProfile)
- **Mục tiêu:** Tạo đồng thời tài khoản `users` và hồ sơ `member_profiles` với hạng New Member (0 điểm) trong 1 Transaction ACID.

### 1. Biểu đồ Luồng Nghiệp Vụ (Flowchart)
```mermaid
flowchart TD
    Start(["Client gửi POST /api/v1/auth/register/otp/verify"]) --> VerifyCode{"Đối soát OTP đăng ký"}
    VerifyCode -- Sai/Hết hạn --> ErrOtp["❌ 400 Bad Request<br>OTP_INVALID / OTP_EXPIRED"]
    VerifyCode -- Đúng --> CheckDuplicate{"SĐT hoặc Email đã đăng ký?"}
    CheckDuplicate -- Đã có --> ErrConflict["❌ 409 Conflict<br>USER_ALREADY_EXISTS"]
    CheckDuplicate -- Chưa có --> TxBegin["Bắt đầu @Transactional ACID"]
    TxBegin --> CreateUser["Tạo UserJpaEntity (role: CUSTOMER, phoneVerified: true)"]
    CreateUser --> QueryTier["Truy vấn TierDefinitions tìm tier_order = 1 (New Member)"]
    QueryTier --> CreateProfile["Tạo MemberProfileJpaEntity (0 điểm, barcode token, dateOfBirth)"]
    CreateProfile --> TxCommit["Commit Transaction"]
    TxCommit --> GenJwt["JwtTokenProvider phát sinh TokenPair"]
    GenJwt --> Success(["✅ 201 Created<br>TokenPair + User Info + Tier New Detail"])

    classDef ok fill:#064e3b,stroke:#10b981,stroke-width:2px,color:#ecfdf5;
    classDef fail fill:#450a0a,stroke:#ef4444,stroke-width:2px,color:#fef2f2;
    class Success ok;
    class ErrOtp,ErrConflict fail;
```

### 2. Biểu đồ Trình Tự Clean Architecture (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as AuthController
    participant UC as VerifyRegisterOtpUseCase
    participant PortS as OtpStoragePort
    participant UserRepo as UserRepository
    participant ProfileRepo as MemberProfileRepository
    participant TierRepo as TierDefinitionRepository
    participant Jwt as JwtTokenProviderService
    participant DB as PostgreSQL

    C->>Ctrl: POST /api/v1/auth/register/otp/verify (phone, email, otpCode)
    Ctrl->>UC: execute(VerifyRegisterOtpRequest)
    UC->>PortS: validateOtp(tenantId, recipient, otpCode)
    alt OTP sai hoặc hết hạn
        UC-->>Ctrl: throw BusinessException(OTP_INVALID)
        Ctrl-->>C: 400 Bad Request
    end
    UC->>UserRepo: existsByTenantIdAndPhoneNumber / Email
    alt Tài khoản đã tồn tại
        UC-->>Ctrl: throw BusinessException(USER_ALREADY_EXISTS)
        Ctrl-->>C: 409 Conflict
    end
    Note over UC,DB: Thực thi trong @Transactional
    UC->>UserRepo: save(UserJpaEntity)
    UserRepo->>DB: INSERT INTO users ...
    UC->>TierRepo: findByTenantIdAndTierOrder(tenantId, 1)
    TierRepo->>DB: SELECT * FROM tier_definitions WHERE tier_order = 1
    DB-->>TierRepo: Tier NewJpaEntity
    UC->>ProfileRepo: save(MemberProfileJpaEntity)
    ProfileRepo->>DB: INSERT INTO member_profiles (0 pts, tier_id...)
    UC->>Jwt: generateTokenPair(user)
    UC-->>Ctrl: VerifyRegisterOtpResponse (tokens, user, loyalty)
    Ctrl-->>C: 201 Created (ApiResponse)
```

---

## Use Case 4: Cấp Lại Token - Xoay Vòng Refresh Token (`RefreshTokenUseCase`)

- **API:** `POST /api/v1/auth/refresh-token`
- **Mục tiêu:** Kiểm tra Refresh Token JWT hợp lệ, đối soát Redis Blacklist, cấp phát cặp Access Token và Refresh Token mới (Token Rotation).

### 1. Biểu đồ Luồng Nghiệp Vụ (Flowchart)
```mermaid
flowchart TD
    Start(["Client gửi POST /api/v1/auth/refresh-token"]) --> ValidateJwt{"Kiểm tra chữ ký & hạn Refresh Token"}
    ValidateJwt -- Hết hạn/Sai --> ErrExp["❌ 401 Unauthorized<br>REFRESH_TOKEN_EXPIRED"]
    ValidateJwt -- Hợp lệ --> CheckBlacklist{"Token có trong Redis Blacklist?"}
    CheckBlacklist -- Có --> ErrRevoked["❌ 401 Unauthorized<br>TOKEN_REVOKED"]
    CheckBlacklist -- Không --> CheckType{"Claim type == 'refresh'?"}
    CheckType -- Không --> ErrType["❌ 401 Unauthorized<br>INVALID_CREDENTIALS"]
    CheckType -- Đúng --> RevokeOld["Đưa Refresh Token cũ vào Redis Blacklist"]
    RevokeOld --> GenPair["Cấp phát cặp Access & Refresh Token mới"]
    GenPair --> Success(["✅ 200 OK<br>TokenPair mới (Token Rotation)"])

    classDef ok fill:#064e3b,stroke:#10b981,stroke-width:2px,color:#ecfdf5;
    classDef fail fill:#450a0a,stroke:#ef4444,stroke-width:2px,color:#fef2f2;
    class Success ok;
    class ErrExp,ErrRevoked,ErrType fail;
```

### 2. Biểu đồ Trình Tự Clean Architecture (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as AuthController
    participant UC as RefreshTokenUseCase
    participant Jwt as JwtTokenProviderService
    participant Blacklist as TokenBlacklistPort
    participant Redis as Redis 7

    C->>Ctrl: POST /api/v1/auth/refresh-token (refreshToken)
    Ctrl->>UC: execute(RefreshTokenRequest)
    UC->>Jwt: validateAndExtractClaims(refreshToken)
    alt Token không hợp lệ hoặc hết hạn
        UC-->>Ctrl: throw BusinessException(REFRESH_TOKEN_EXPIRED)
        Ctrl-->>C: 401 Unauthorized
    end
    UC->>Blacklist: isBlacklisted(jti)
    Blacklist->>Redis: EXISTS auth:blacklist:{jti}
    alt Nằm trong danh sách đen
        UC-->>Ctrl: throw BusinessException(TOKEN_REVOKED)
        Ctrl-->>C: 401 Unauthorized
    end
    UC->>Blacklist: blacklistToken(jti, remainingTtl)
    Blacklist->>Redis: SET auth:blacklist:{jti} = REVOKED
    UC->>Jwt: generateTokenPair(userId, tenantId, role)
    UC-->>Ctrl: RefreshTokenResponse (newAccessToken, newRefreshToken)
    Ctrl-->>C: 200 OK (ApiResponse)
```

---

## Use Case 5: Đăng Xuất & Thu Hồi Token Vào Redis Blacklist (`LogoutUseCase`)

- **API:** `POST /api/v1/auth/logout`
- **Mục tiêu:** Thu hồi Access Token và Refresh Token ngay lập tức bằng cách ghi `jti` vào Redis Blacklist.

### 1. Biểu đồ Luồng Nghiệp Vụ (Flowchart)
```mermaid
flowchart TD
    Start(["Client gửi POST /api/v1/auth/logout"]) --> ExtractAuth{"Trích xuất Authorization Bearer token"}
    ExtractAuth -- Có token --> ParseJti["Trích xuất JTI & tính remaining TTL của Access Token"]
    ParseJti --> AddBlacklist["Lưu auth:blacklist:{jti} vào Redis với TTL còn lại"]
    ExtractAuth -- Không có token --> CheckRefresh{"Có refreshToken trong body?"}
    AddBlacklist --> CheckRefresh
    CheckRefresh -- Có --> AddRefreshBlacklist["Lưu jti của Refresh Token vào Redis Blacklist"]
    CheckRefresh -- Không --> Done(["✅ 200 OK<br>Đăng xuất thành công, phiên đã bị hủy"])
    AddRefreshBlacklist --> Done

    classDef ok fill:#064e3b,stroke:#10b981,stroke-width:2px,color:#ecfdf5;
    class Done ok;
```

### 2. Biểu đồ Trình Tự Clean Architecture (Sequence Diagram)
```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant Ctrl as AuthController
    participant UC as LogoutUseCase
    participant Jwt as JwtTokenProviderService
    participant Blacklist as TokenBlacklistPort
    participant Redis as Redis 7

    C->>Ctrl: POST /api/v1/auth/logout (Authorization: Bearer <token>)
    Ctrl->>UC: execute(authHeader, LogoutRequest)
    opt Header có Bearer token
        UC->>Jwt: extractJtiAndRemainingTtl(token)
        UC->>Blacklist: blacklistToken(accessJti, remainingMs)
        Blacklist->>Redis: SET auth:blacklist:{accessJti} = REVOKED (TTL)
    end
    opt Body có refreshToken
        UC->>Jwt: extractJtiAndRemainingTtl(refreshToken)
        UC->>Blacklist: blacklistToken(refreshJti, remainingMs)
        Blacklist->>Redis: SET auth:blacklist:{refreshJti} = REVOKED (TTL)
    end
    UC-->>Ctrl: void
    Ctrl-->>C: 200 OK ("Đăng xuất thành công")
```

