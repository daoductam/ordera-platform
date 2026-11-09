# Order Module Implementation Walkthrough

**Date:** 2026-11-09  
**Status:** Completed & Verified  
**Package:** `com.ordera.modules.order`  
**Test Results:** 234/234 Tests Passed (26/26 Order Module Tests Passed)

---

## 1. Overview & Objective
This walkthrough documents the end-to-end backend implementation of the **Order Module** for Ordera Multi-Tenant Loyalty & Ordering Platform. The module adheres to **Clean Architecture (4 layers)**, strict Multi-Tenant isolation (`TenantContextHolder`), Redisson distributed concurrency locks, 24-hour Redis idempotency keys, and zero magic numbers/strings.

---

## 2. Architecture & Layer Mapping

```
com.ordera.modules.order
├── domain/
│   ├── constant/
│   │   └── OrderConstants.java          # Concentrated business constants (VAT 15%, TTL, timeouts, scales)
│   ├── exception/
│   │   └── OrderErrorCode.java          # RFC 7807 business error codes
│   └── model/
│       ├── Cart.java & CartItem.java    # Domain cart models
│       ├── Order.java & OrderItem.java  # Domain order entities
│       ├── OrderItemModifier.java       # Modifiers/customizations
│       ├── OrderStatus.java             # PENDING, PREPARING, READY, COMPLETED, CANCELLED
│       ├── PaymentStatus.java           # PENDING, PAID, REFUNDED, FAILED
│       └── PickupMethod.java            # COUNTER, TABLE
├── application/
│   ├── port/in/
│   │   ├── CartService.java             # Cart operations interface
│   │   ├── PreviewCheckoutCalculationUseCase.java
│   │   ├── CheckoutOrderUseCase.java
│   │   ├── GetMyOrdersUseCase.java
│   │   ├── GetOrderDetailUseCase.java
│   │   ├── ReorderPastOrderUseCase.java
│   │   └── SendEReceiptEmailUseCase.java
│   └── service/
│       ├── CartServiceImpl.java         # Redis Cache (TTL 7d) + Postgres sync
│       ├── PreviewCheckoutCalculationUseCaseImpl.java
│       ├── CheckoutOrderUseCaseImpl.java # Orchestrator Pattern (<=35 lines execute())
│       ├── GetMyOrdersUseCaseImpl.java   # Active vs Past order tabs + Pagination
│       ├── GetOrderDetailUseCaseImpl.java# Detailed view + 3-step kitchen timeline
│       ├── ReorderPastOrderUseCaseImpl.java
│       └── SendEReceiptEmailUseCaseImpl.java
├── infrastructure/
│   ├── adapter/
│   │   ├── RedisCartStorageAdapter.java # Redis hash storage with 7-day TTL
│   │   └── JavaMailReceiptAdapter.java  # PDF 1.4 byte stream generator & JavaMailSender
│   ├── mapper/
│   │   └── OrderDtoMapper.java          # MapStruct DTO/Entity mappings
│   └── persistence/repository/
│       ├── OrderJpaRepository.java & OrderRepository.java
│       ├── OrderItemJpaRepository.java & OrderItemRepository.java
│       ├── OrderItemModifierJpaRepository.java & OrderItemModifierRepository.java
│       └── CartJpaRepository.java & CartItemJpaRepository.java
└── presentation/
    ├── constant/
    │   └── OrderMessages.java           # Centralized MSG_* and ERR_* messages
    ├── controller/
    │   ├── CartController.java          # APIs 6.1 - 6.4
    │   └── OrderController.java         # APIs 6.5 - 6.10
    └── dto/                             # Clean request/response DTO contracts
```

---

## 3. Detailed Task Walkthrough (Groups 1 - 5)

### Group 1: Domain & Math Foundation
- **`OrderConstants.java`:** Defines centralized business constants:
  - `VAT_RATE = 0.15`, `VAT_DIVISOR = 1.15`
  - `CART_TTL_DAYS = 7`, `IDEMPOTENCY_KEY_TTL_HOURS = 24`
  - `MAX_ALLERGY_NOTE_LENGTH = 200`
  - `ORDER_CODE_PREFIX = "ORD-"`, `QUEUE_NUMBER_PREFIX = "#"`
  - `DECIMAL_SCALE = 2` with `RoundingMode.HALF_UP`
  - Redisson lock timeouts (`5s` acquire, `10s` lease).
- **`OrderErrorCode.java`:** Standardized RFC 7807 error codes (`CART_EMPTY`, `MINIMUM_ORDER_VALUE_NOT_MET`, `IDEMPOTENT_OPERATION_CONFLICT`, `STOCK_UNAVAILABLE`, `ORDER_NOT_FOUND`, `INVALID_ORDER_STATUS`) throwing `BusinessException`.
- **`OrderCalculationUtils.java`:** Precise financial math:
  - Subtotal = $\sum (\text{itemPrice} + \text{modifierTotal}) \times \text{quantity}$
  - Total = $\max(0, \text{subtotal} - \text{discount}) + \text{tip}$
  - VAT 15% = $\text{Total} \times \frac{15}{115}$ rounded to 2 decimal places (`HALF_UP`)
  - Points to Earn = $\lfloor (\text{subtotal} - \text{discount}) \times \text{tierRate} \rfloor$ (Tips are strictly excluded).

### Group 2: Cart System
- **`CartService` & `CartServiceImpl`:**
  - Fast Redis read/write with automatic 7-day TTL expiration.
  - Background PostgreSQL write-through synchronization.
  - Modifiers hash calculation to correctly distinguish identical products with different modifier selections.
  - Validation of allergy note length ($\le 200$ chars) throwing `OrderErrorCode.INVALID_ORDER_STATUS` / validation errors when violated.
- **`CartController`:**
  - 6.1 `GET /api/v1/cart`: Retrieves active cart.
  - 6.2 `POST /api/v1/cart/items`: Adds item with modifiers.
  - 6.3 `PATCH /api/v1/cart/items/{itemId}` & `DELETE /api/v1/cart/items/{itemId}`: Updates item quantity or removes it.
  - 6.4 `PUT /api/v1/cart/comment`: Updates allergy/order instructions.

### Group 3: Checkout, Concurrency & Transactions
- **`PreviewCheckoutCalculationUseCaseImpl`:**
  - Evaluates discounts from applied vouchers (`PERCENT` vs `FIXED_AMOUNT`).
  - Calculates non-discountable items (gift tumblers).
  - Calculates final VAT, tip, total, and estimated loyalty points to earn.
- **`CheckoutOrderUseCaseImpl` (Orchestrator Pattern):**
  - **Execute Method ($\le 35$ lines):** Delegates each phase to private helpers.
  - **Idempotency:** Checks Redis key `order:idempotency:{key}` with 24-hour TTL; rejects duplicate submissions with `409 Conflict`.
  - **Redisson Distributed Lock:** Acquires lock `lock:order:checkout:{userId}` with 5-second wait, 10-second lease to eliminate race conditions.
  - **ACID Transaction:**
    - Generates human-friendly `order_number` (`#005`) and unique `order_code` (`ORD-YYYYMMDD-XXXX`).
    - Creates order, order items, and item modifiers.
    - Transitions applied voucher status to `USED`.
    - Invokes `AccruePointsUseCase` to create an immutable `ORDER_EARN` ledger entry.
    - Empties Redis cart cache upon successful transaction commit.

### Group 4: Order Tracking, Reorder & E-Receipt
- **`GetMyOrdersUseCaseImpl`:** Queries active (`PENDING`, `PREPARING`, `READY`) vs past (`COMPLETED`, `CANCELLED`) orders with pageable support.
- **`GetOrderDetailUseCaseImpl`:** Returns rich order details, store address, items, financial breakdown, and 3-step kitchen timeline (`RECEIVED` $\rightarrow$ `PREPARING` $\rightarrow$ `READY`).
- **`ReorderPastOrderUseCaseImpl`:** Re-adds past order items and modifiers to the customer's current cart with 1 click.
- **`SendEReceiptEmailUseCaseImpl` & `JavaMailReceiptAdapter`:**
  - Generates valid PDF 1.4 binary stream in-memory.
  - Dispatches multipart email with attachment via Spring `JavaMailSender`.

### Group 5: Presentation & Automated Verification
- **`OrderMessages.java`:** Centralized response strings (`MSG_CART_RETRIEVED`, `MSG_ORDER_CHECKOUT_SUCCESS`, etc.).
- **Unit Tests (26/26 tests passed):**
  - `OrderCalculationUtilsTest`: 9 test cases verifying rounding, zero tips, large discounts, boundary edge cases.
  - `OrderConstantsAndErrorCodesTest`: 3 test cases validating immutability and RFC 7807 codes.
  - `CartServiceTest`: 4 test cases testing add, update, delete, allergy note limit.
  - `PreviewCheckoutCalculationUseCaseTest`: 3 test cases testing discount calculations and point accruals.
  - `CheckoutOrderUseCaseTest`: 3 test cases for lock acquisition, idempotency conflict, and successful ledger accrual.
  - `CartControllerTest`: 2 test cases covering cart endpoints.
  - `OrderControllerTest`: 2 test cases covering checkout preview and orders list pagination.

---

## 4. Full Suite Verification Summary
Execution of full backend test suite:
```powershell
mvn -f backend/pom.xml test
```
**Results:**
- **Tests run:** 234
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Build Status:** SUCCESS
