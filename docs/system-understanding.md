# System Understanding — Inventory Management (Mentor Reference)

> Purpose: Accurate mental model of the existing implementation as verified 2026-09-10.
> Sources: repository code + `AGENTS.md` + `docs/ai-mentor.md` + `Flow.md` + `initail.md` + `quantity-thought.md` + `Otpworkflow.md`.
> Rule: This is a learning project. Do not rewrite code unprompted. Guide, don't generate large implementations.

## 1. Architecture

Layered Spring Boot monolith (Boot 4.1.0, Java 17):
`Controller -> Service interface -> Service impl -> JpaRepository -> PostgreSQL`
plus Redis for OTP, stateless JWT security, manual Mapper components, Lombok.

- No schema migrations. `spring.jpa.hibernate.ddl-auto: update`, `show-sql: true`.
- No event bus / microservices. All calls are synchronous.
- Docs describe a future full flow (OTP -> Order PENDING -> Payment PENDING -> Razorpay -> webhook -> CONFIRMED + SALE transaction) that is **not** in code yet.

## 2. Major Components (implemented vs stub)

Implemented:
- `AuthController` + `AuthServiceImpl` + `OtpServiceImpl` + `JwtServiceImpl`
- `ProductController` + `ProductServiceImpl` + `ProductMapper`
- `CategoryController` (`/api/v1/categorie`) + `CategoryServiceImpl` + `CategoryMapper`
- `UserController` + `UserServiceImpl` (+ `UserMapper` defined but mostly unused)
- `SecurityConfig`, `JwtAuthenticationFilter`, `CustomUserDetails/Service`
- Repos: `UserRepository`, `ProductRepository`, `ProductCategoryRepository`, `OrderRepository`, `OtpCacheRepository`
- `DataInitializer` seeds ADMIN `9763369894`.

Stub / empty:
- `OrderController`: `@Controller` (not `@RestController`), `getOrders()` has no return.
- `OrderServiceImpl`: `createOrder` incomplete (`new Order()` missing `;`), returns `null`; `updateOrderStatus` returns `null`.
- `OrderMapper`: no `@Component`, `toOrderItemResponse` fields commented out, never injected.
- `OrderItemService` + `OrderItemServiceImpl`: empty, impl not a bean.
- `OrderItemController`, `AddressController`: only comments.
- No repo/service/controller for `Payment`, `StockTransaction`, `Address`, `OrderItem`. No `InventoryService`. No Razorpay code.

## 3. Entities & Relationships

- `User(users, UUID)`: email nullable unique, phoneNumber non-null unique, name, role default USER, createdAt via PrePersist. OneToMany orders CASCADE ALL orphanRemoval.
- `ProductCategory(category, UUID AUTO)`: name unique, createdAt/updatedAt. No back-reference.
- `Product(product singular, UUID)`: name, description TEXT, price(10,2), ManyToOne LAZY category non-null, quantity default 0, active default true, Creation/UpdateTimestamp.
- `Order(orders, UUID)`: status nullable, discount/tax/shipping_charges/subtotal/grand_total(non-null)/paymentStatus(non-null), ManyToOne LAZY user non-null, OneToMany orderItems CASCADE ALL orphanRemoval, OneToMany payments CASCADE ALL, timestamps.
- `OrderItem(order_item singular, UUID)`: ManyToOne order non-null, ManyToOne product non-null, quantity/unitPrice/discount/tax/lineTotal non-null, timestamps. Intended as price snapshot.
- `StockTransaction(stock_transactions, UUID)`: ManyToOne product non-null, ManyToOne order nullable, ManyToOne orderItem via order_items_id, type non-null, quantityChange, createdAt only, ManyToOne createdBy User nullable, reference.
- `Payment(payments, UUID)`: ManyToOne order non-null, amount, paymentMethod plain Column (missing Enumerated), transactionReference, status Enumerated, createdAt only.
- `Address(address singular, UUID AUTO)`: ManyToOne user non-null (EAGER default), line1/line2/city/state/postalCode/country, isDefault false, timestamps.
- `OtpCache(RedisHash otp_verification, TTL)`: phoneNumber Id, otpCode, expirationInSeconds. Defined but unused — real OTP uses RedisTemplate<String,String> key `otp:phone`.
- Enums: Role USER/ADMIN; OrderStatus PENDING/CONFIRMED/PROCESSING/SHIPPED/DELIVERED/CANCELLED; PaymentStatus PENDING/CONFIRMED/FAILED/REFUNDED; PaymentMethod COD/UPI/CARD/NET_BANKING; StockTransactionType SALE/PURCHASE/RETURN/ADJUSTMENT/DAMAGE.

## 4. Request / Business Flows (actual code)

- OTP: POST send-otp -> generate SecureRandom 6-digit -> Redis `otp:phone` 5min (printed to stdout, no SMS). POST verify-otp -> compare + delete on success -> find-or-create User(New User/USER) -> JWT subject=phone + role claim -> AuthResponse.
- JWT: permit `/api/v1/auth/**`, else authenticated. Filter extracts Bearer, extractSubject, loads UserDetails, sets ROLE_* auth. No explicit isTokenValid check before load.
- Product: GET all/byId any auth; POST/PATCH/DELETE ADMIN + Valid.
- Category: all incl. GETs ADMIN-only.
- User: GET/PATCH /me via Authentication.getName(); admin GET/{id}, GET /, PUT/{id}, DELETE/{id}. GET / has unused @PathVariable bug.
- Order/Payment/Inventory: no live flow.

## 5. Data Movement

DTO in -> Service -> Mapper.toEntity/toResponse -> JpaRepository entity -> Postgres -> Response DTO. Entities never leave except broken OrderController. ProductMapper dereferences `product.getCategory().getId()` (LAZY proxy risk outside TX).

## 6. Persistence

JPA/Hibernate -> Postgres; RedisTemplate for OTP. ddl-auto update, UUID gens (UUID vs AUTO split), derived queries only (existsByNameIgnoreCase, findByPhoneNumber, findByName). Product.quantity only set at creation, never adjusted. No writes to StockTransaction/Payment/OrderItem/Address.

## 7. Transactions

Only single-entity writes: ProductServiceImpl class readOnly + method write; CategoryServiceImpl only create; UserServiceImpl two updates. No TX on Auth verify, no TX on Order, no locking/version, no atomic Order+Payment+stock block.

## 8. Currently Incomplete / Known Inconsistencies

- Order/OrderItem/StockTransaction/Payment/Address/InventoryService logic missing (per AGENTS.md constraint — do not implement unless asked).
- DTO problems: CreateOrderRequest no Lombok/getters; duplicate CreateOrderItemRequest (order POJO vs orderItem record); Category update ignores path id; User list signature bug.
- No GlobalExceptionHandler; most DTOs lack validation; Category update self-conflict bug; hard delete Product; PaymentMethod missing Enumerated; naming drift (categorie, shipping_charges, singular/plural tables); secrets hardcoded; yaml vs compose host mismatch; only contextLoads test.
- Design docs (Flow/initail/quantity-thought) propose ledger (Product.quantity cache + StockTransaction statement) + snapshot OrderItems + post-payment deduction + conditional update for concurrency — not yet reflected in code.
