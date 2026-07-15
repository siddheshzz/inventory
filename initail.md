1. User Authentication
   User
   │
   ▼
   Enter Phone Number
   │
   ▼
   POST /auth/login
   │
   ▼
   Generate OTP
   (Store in Redis/DB with expiry)
   │
   ▼
   SMS Provider
   │
   ▼
   User enters OTP
   │
   ▼
   POST /auth/verify
   │
   ▼
   OTP Valid?
   │
   Yes
   │
   ▼
   Find/Create User
   │
   ▼
   Generate JWT
   │
   ▼
   Return Access Token

After this, every request includes:

Authorization: Bearer JWT
2. Browse Products
   GET /products

Only reads.

No inventory changes.

Stock displayed is derived from:

SUM(stock_transactions.quantity_change)

or a cached value if you optimize later.

3. User Creates an Order

Suppose user buys:

Laptop x2
Mouse x1

Client sends:

POST /orders

{
"shippingAddressId": "...",
"items": [
{
"productId": "...",
"quantity": 2
},
{
"productId": "...",
"quantity": 1
}
]
}
OrderService
Step 1

Validate JWT

↓

Find User

Step 2

Load Products

SELECT product
WHERE id IN (...)
Step 3

Check inventory

InventoryService

↓

available quantity?

If insufficient

400 Bad Request

No order created.

Step 4

Calculate

subtotal

discount

tax

shipping

grand total
Step 5

Create Order

Order

status = PAYMENT_PENDING

payment_status = PENDING

Save.

Step 6

Create OrderItems

For each product

OrderItem

order_id

product_id

quantity

price

discount

tax

Save all.

Nothing has happened to inventory yet.

No StockTransaction.

Current DB

Order

PAYMENT_PENDING

↓

OrderItems

↓

No Payment

↓

No StockTransaction


---

# Why create OrderItems before payment?

Because Razorpay needs an amount.

Amount comes from


OrderItems


You cannot charge without knowing exactly what the customer is buying.

---

# 4. Payment Initiation

OrderService now calls


PaymentService


PaymentService

↓

Razorpay


Create Razorpay Order


Razorpay returns


razorpay_order_id


Create Payment


Payment

status=PENDING

gateway_order_id=...


Return


razorpay_order_id


Frontend opens Razorpay Checkout.

---

Current DB


Order


↓


OrderItems


↓


Payment
(PENDING)


---

# 5. User Pays

Customer completes payment.

Razorpay sends


POST /payments/webhook


This endpoint is **not** called by your frontend. It is called by Razorpay's servers.

Example payload:


payment_id

order_id

signature

status


---

# Webhook Flow

PaymentController

↓

Verify Signature

↓

Prevent duplicate processing (idempotency)

↓

Find Payment

↓

If already SUCCESS

return 200

Else continue.

---

Update Payment


SUCCESS


Update Order


payment_status = PAID

status = CONFIRMED


Now inventory changes happen.

---

# 6. InventoryService

Called only after successful payment.


InventoryService

↓

For every OrderItem


Example

Laptop x2

Create


StockTransaction

product=laptop

type=SALE

quantity_change=-2

order_id=...

order_item_id=...


Mouse

Create


StockTransaction

quantity_change=-1


No Product update.

No quantity field update.

Only insert StockTransaction rows.

This creates a complete audit trail.

---

Inventory becomes


Old Stock

100


↓

SALE


-2


↓

Remaining

98

Derived by aggregation.

---

# 7. Admin Adds New Inventory

Suppose warehouse receives


50 laptops


Admin


POST /inventory/adjustments


Body

```json
{
  "productId": "...",
  "quantity": 50,
  "reason": "PURCHASE"
}

InventoryService

↓

Create

StockTransaction

+50

type=PURCHASE

Done.

Again

No Product.quantity update.

8. Customer Cancels Before Payment
Order

status=CANCELLED

Delete nothing.

Keep history.

Payment stays

FAILED

or

CANCELLED

Inventory unchanged.

9. Refund

Admin

↓

Refund

↓

Razorpay Refund API

↓

Webhook

↓

Payment

REFUNDED

↓

Order

REFUNDED

↓

InventoryService

↓

Create

StockTransaction

RETURN

+2

Inventory restored.

10. Damage

Warehouse reports

Laptop damaged

Inventory endpoint

POST /inventory/adjustments
quantity=-1

reason=DAMAGE

Creates

StockTransaction

type=DAMAGE

-1
11. Manual Adjustment

Inventory count mismatch

Expected

100

Actual

98

Admin

↓

POST

/inventory/adjustments

Reason

ADJUSTMENT

StockTransaction

-2
So what is the Inventory endpoint for?

It is not used during customer checkout.

It exists for inventory management operations performed by admins or warehouse staff:

Receiving new stock (PURCHASE)
Recording damaged items (DAMAGE)
Correcting inventory counts (ADJUSTMENT)
Optionally recording manual returns

Customer sales are recorded automatically by OrderService after payment succeeds.

Complete lifecycle
Login
      │
      ▼
JWT
      │
      ▼
Browse Products
      │
      ▼
Create Order
      │
      ▼
Create OrderItems
      │
      ▼
Create Payment
      │
      ▼
Razorpay Checkout
      │
      ▼
Webhook
      │
      ▼
Verify Signature
      │
      ▼
Update Payment
      │
      ▼
Update Order
      │
      ▼
InventoryService
      │
      ▼
Create StockTransactions
      │
      ▼
Order Completed
One architectural improvement

Consider introducing a domain event after payment succeeds. Instead of PaymentService directly calling InventoryService, publish an event such as PaymentSucceeded or OrderPaid. An event handler can then:

Create StockTransaction records.
Send confirmation notifications.
Trigger invoice generation.
Update analytics.

This decouples services and makes the system easier to extend. Even if you implement it synchronously for now, modeling the flow around events reflects an architecture commonly used in production systems as they grow.

Today 1:25 PM
so code wise what happens after order is created witha status pending then order status is updated and payment record is creted post verification of sig. Now what do i created order item record or stock transaction record or what and what is inventotry here cause i dont have any inventory object or model here

This is exactly the question a backend engineer should ask. You're now thinking about business flow, not just tables.

One thing first:

Inventory is not an entity in your design. It is a domain/service, not a database table.

That is actually a good design.

Your current entities
User
Category
Product
Address
Order
OrderItem
Payment
StockTransaction

Notice there is no Inventory table.

That's fine because

Current Stock
=
SUM(quantity_change)
FROM StockTransaction
WHERE product_id=...

or

Product.quantity (cached value)

depending on implementation.

So InventoryService simply manages stock.

It doesn't need an Inventory entity.

Complete Production Flow

Imagine customer buys

2 Apples
3 Mangoes
1. Login
POST /auth/login

OTP verified

↓

JWT issued

Nothing unusual.

2. User clicks Checkout

Frontend sends

POST /orders

{
 addressId,
 items:[
   {productId:1, qty:2},
   {productId:5, qty:3}
 ]
}

Inside OrderService

Validate
Address belongs to user

Products exist

Enough stock

InventoryService is called

inventoryService.checkAvailability()

Notice

No stock is deducted yet.

Only checking.

Then

Create

Order
status=PENDING_PAYMENT

Now create

OrderItems
Apple x2

Mango x3

OrderItems ARE created before payment.

This surprises many juniors.

But this is industry standard.

Why?

Because Payment Gateway needs

amount

currency

items

metadata

Those items already exist.

Database now

Order

PENDING_PAYMENT
OrderItems

Apple

Mango

No stock changed.

No payment yet.

3. Payment Record

Create

Payment

status=PENDING

Store

orderId

gateway=Razorpay

amount

Now database

Order

Payment

OrderItems
4. Create Razorpay Order

Backend calls Razorpay

amount

receipt

notes

Gets

razorpay_order_id

Store it

inside Payment.

Return

orderId

razorpayOrderId

key

to frontend.

5. User Pays

Frontend opens Razorpay Checkout.

Customer pays.

6. Razorpay returns

Frontend receives

paymentId

signature

orderId

Backend receives

POST /payments/verify

Now verify signature.

If invalid

Stop.

Payment remains FAILED.

If valid

Start ONE DATABASE TRANSACTION.

This is extremely important.

Inside ONE transaction

Update

Payment

SUCCESS

Update

Order

CONFIRMED

Now comes Inventory.

InventoryService

Call

inventoryService.reserveStock(order)

This service loops through OrderItems.

for each item

Example

Apple

qty

2

Decrease Product quantity

100

↓

98

Create

StockTransaction

SALE

-2

productId

orderId

orderItemId

Then

Second item

Mango

50

↓

47

Create

StockTransaction

SALE

-3

Done.

Commit.

Now database becomes

Order
CONFIRMED
Payment
SUCCESS
Product
qty updated
StockTransaction
SALE

Notice

OrderItem was NOT created here.

It already existed.

Only StockTransaction gets created after payment.

Why?

Because unpaid carts should not reduce inventory.

Imagine

100 users click Pay.

Only 2 actually pay.

If you reduce stock before payment

everything breaks.

So what does InventoryService actually do?

Think of it as

InventoryService

Responsibilities

checkAvailability()

deductStock()

addStock()

returnStock()

adjustStock()

stockHistory()

Notice

No Inventory entity.

Just business logic.

Internally

deductStock()

does

Product.quantity -= qty

+

StockTransaction insert
Purchase Flow

Admin buys stock.

POST /inventory/purchase

InventoryService

Product.quantity +=100

Insert

StockTransaction

PURCHASE

+100
Return Flow

Customer returns.

POST /returns

InventoryService

Product.quantity +=2

Insert

RETURN

+2
Damage

Admin

POST /inventory/damage
Product.quantity -=5

Insert

DAMAGE

-5

Everything becomes auditable.

Final Production Timeline
OTP Login
      │
      ▼
JWT Generated
      │
      ▼
POST /orders
      │
      ▼
Validate products
      │
      ▼
Create Order (PENDING_PAYMENT)
      │
      ▼
Create OrderItems
      │
      ▼
Create Payment (PENDING)
      │
      ▼
Create Razorpay Order
      │
      ▼
Frontend Payment
      │
      ▼
Webhook / Verify Signature
      │
      ▼
DB Transaction Starts
      │
      ├── Payment → SUCCESS
      ├── Order → CONFIRMED
      ├── Product.quantity -= ordered qty
      ├── Create StockTransaction(SALE)
      └── Commit
      │
      ▼
Order Completed
One recommendation to make it even more production-grade

Instead of updating Product.quantity directly from multiple requests, perform the stock deduction atomically (for example, using optimistic locking with a @Version field or a conditional update such as UPDATE product SET quantity = quantity - ? WHERE id = ? AND quantity >= ?). This prevents overselling when two customers buy the last item at nearly the same time. If the update affects zero rows, you know stock became unavailable and can fail the order gracefully.

This is the kind of concurrency handling you'd typically see in a senior-level production backend.

create a context summary of this chat in detail for another seesion

Here is a detailed context summary you can paste into a new ChatGPT session so it has full context of the project.

Project Context

I am building a production-level, industry-standard Spring Boot e-commerce backend as a portfolio project. The goal is to design and implement it the way a senior backend engineer would, focusing on clean architecture, scalability, maintainability, and real-world practices.

The stack is approximately:

Java
Spring Boot
Spring Security
Spring Data JPA (Hibernate)
PostgreSQL
JWT Authentication
OTP Login
Razorpay Payment Gateway
REST APIs
UUID primary keys
Layered Architecture

I am intentionally trying to avoid "tutorial code" and instead follow production-grade patterns.

Database Design (Current)

Entities:

User
Address
Category
Product
Order
OrderItem
Payment
StockTransaction

All tables contain production metadata like:

created_at
updated_at
deleted_at (soft delete where applicable)

User also contains:

role
User

Fields include

id (UUID)
phone number
email
name
role
timestamps

Relationships

One User -> Many Orders
One User -> Many Addresses
Address

Fields

id
user_id
address_line1
address_line2
city
state
pincode
timestamps

Relationship

Many addresses belong to one user.

Orders reference shipping_address_id.

Category

Simple category table

Fields

id
name
timestamps

Relationship

One category -> many products.

Product

Fields include

id
category_id
name
description
price
quantity (cached/derived stock)
timestamps

Product belongs to Category.

Product has many StockTransactions.

Product has many OrderItems.

Order

Fields include

id
user_id
shipping_address_id

Financial fields

subtotal
discount
tax
shipping_charge
grand_total

Statuses

status
payment_status

Timestamps

Relationship

Order belongs to User.

Order has many OrderItems.

Order has one Payment.

OrderItem

Fields

id
order_id
product_id
quantity
unit_price
discount_type
discount_value
tax_amount
line_total

Relationship

Many OrderItems belong to one Order.

Each OrderItem references one Product.

Payment

Fields

id
order_id
amount
payment_method
status
transaction_id
razorpay_order_id
timestamps

Relationship

One Payment belongs to one Order.

Payment is NOT directly connected to User.

StockTransaction

Purpose

Acts as inventory ledger/audit log.

Fields

id
product_id
order_id (nullable)
order_item_id (nullable)

Enum type

SALE
PURCHASE
RETURN
DAMAGE
ADJUSTMENT

Other fields

quantity_change
created_by
reference
timestamps

Relationship

Many StockTransactions belong to Product.

Can optionally reference Order and OrderItem.

Inventory Design

There is NO Inventory entity.

This is intentional.

Inventory is treated as a domain/service rather than a database table.

Current stock is represented by Product.quantity.

StockTransaction exists for complete audit history.

InventoryService is responsible for

checking stock
deducting stock
adding stock
adjustments
returns
purchase stock
damage stock

InventoryService internally updates Product.quantity and creates StockTransaction entries.

Authentication Flow

Authentication is phone-number based.

Flow

User enters phone number

↓

OTP sent

↓

OTP verified

↓

JWT generated

↓

User authenticated

No passwords.

JWT secures all protected APIs.

Order Lifecycle

The agreed production flow is:

1.

User logs in using OTP.

Receives JWT.

2.

User selects products.

Frontend calls

POST /orders

with

addressId
items
3.

OrderService validates

user
address ownership
product existence
inventory availability

InventoryService only checks stock.

No stock deduction happens yet.

4.

Create Order

Status

PENDING_PAYMENT

5.

Immediately create OrderItems.

Reason

Payment gateway requires complete order information.

Industry standard is to create OrderItems before payment.

6.

Create Payment

Status

PENDING

Store

order_id
amount
gateway
7.

Backend creates Razorpay Order.

Store razorpay_order_id.

Return Razorpay information to frontend.

8.

Customer completes payment.

Frontend sends

paymentId

signature

orderId

9.

Backend verifies Razorpay signature.

If invalid

Payment fails.

Nothing changes.

10.

If signature is valid

Begin ONE database transaction.

Inside transaction

Update Payment

SUCCESS

Update Order

CONFIRMED

Call InventoryService.

InventoryService loops through OrderItems.

For every OrderItem

Decrease Product.quantity

Create StockTransaction

Type

SALE

Commit transaction.

Important

OrderItems are NOT created after payment.

They are created before payment.

StockTransactions are created after successful payment.

StockTransaction Usage

SALE

Created after successful payment.

Negative quantity.

PURCHASE

Admin purchases inventory.

Positive quantity.

RETURN

Customer returns product.

Positive quantity.

DAMAGE

Admin marks damaged stock.

Negative quantity.

ADJUSTMENT

Manual stock correction.

Concurrency Consideration

Senior-level recommendation

Avoid simple

Product.quantity--

Instead use

Optimistic locking

(@Version)

or

Conditional update

Example

UPDATE product

SET quantity = quantity - ?

WHERE

id = ?

AND quantity >= ?

This prevents overselling.

REST API Structure
Auth

POST /auth/send-otp

POST /auth/verify-otp

POST /auth/refresh

POST /auth/logout

User

GET /users/me

PUT /users/me

Address

GET /addresses

POST /addresses

PUT /addresses/{id}

DELETE /addresses/{id}

Category

GET /categories

GET /categories/{id}

POST /categories

PUT /categories/{id}

DELETE /categories/{id}

Product

GET /products

GET /products/{id}

POST /products

PUT /products/{id}

DELETE /products/{id}

Order

POST /orders

GET /orders

GET /orders/{id}

PATCH /orders/{id}/cancel

PATCH /orders/{id}/status

Payment

POST /payments/verify

POST /payments/webhook

GET /payments/{id}

Inventory

GET /inventory/products/{id}

POST /inventory/purchase

POST /inventory/adjust

POST /inventory/damage

POST /inventory/return

GET /inventory/transactions

GET /inventory/transactions/{id}

Architecture

Following layered architecture

Controller

↓

Service Interface

↓

Service Implementation

↓

Repository

↓

Database

DTOs are used between Controller and Service.

Entities are never exposed directly.

Repository Pattern

Repositories use Spring Data JPA.

Interfaces extend JpaRepository.

No custom implementation unless needed.

Service Layer

Every service has

Example

OrderService

OrderServiceImpl

Reason

Allows

loose coupling
easier testing
future multiple implementations
dependency inversion
N+1 Query Discussion

Covered the N+1 problem.

Example

Loading Orders

then loading User for each Order

then loading OrderItems for each Order

causes many SQL queries.

Avoid using

JOIN FETCH
EntityGraph
DTO projections
batch fetching

Do not blindly use EAGER fetching.

Prefer LAZY with explicit fetch strategies.

UML/Class Diagram Plan

Next phase of the project is to create

Class Diagram
Interfaces
Repository structure
Service Interfaces
Service Implementations
Controller structure

The goal is for the UML to reflect a real production Spring Boot architecture rather than just mirroring the ERD.

I want guidance and reviews from the perspective of a senior backend engineer, emphasizing clean design, SOLID principles, scalability, maintainability, and production-readiness rather than just making the project "work."