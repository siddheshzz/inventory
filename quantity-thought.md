# Production-Grade E-Commerce Inventory & Order Processing Architecture

---

# High-Level Architecture

```text
                                  Customer
                                      │
                                      ▼
                           Create Order Request
                                      │
                                      ▼
                              OrderController
                                      │
                                      ▼
                               OrderService
                                      │
         ┌────────────────────────────┼────────────────────────────┐
         │                            │                            │
         ▼                            ▼                            ▼
  Product Repository          InventoryService            Pricing Engine
         │                            │                            │
         ▼                            ▼                            ▼
 Read Current Product         Manage Stock Levels       Calculate Pricing
 Information                  & Inventory History      Tax & Discounts
         │                            │                            │
         └────────────────────────────┼────────────────────────────┘
                                      │
                                      ▼
                            Create Order & OrderItems
                                      │
                                      ▼
                             Persist to Database
```

---

# Inventory Architecture

The inventory system intentionally stores the stock in **two different places** because they solve two completely different business problems.

```text
                        Inventory Database
                                │
          ┌─────────────────────┴─────────────────────┐
          │                                           │
          ▼                                           ▼

    Product Table                           StockTransaction Table
┌───────────────────────────┐        ┌──────────────────────────────┐
│ id                        │        │ id                           │
│ name                      │        │ product_id                   │
│ category                  │        │ transaction_type             │
│ selling_price             │        │ quantity_change              │
│ quantity (Current Stock)  │        │ reference_id                 │
│ status                    │        │ remarks                      │
│ created_at                │        │ created_at                   │
└───────────────────────────┘        └──────────────────────────────┘

Current Snapshot                     Complete Inventory History
```

---

# Why Store Both?

```text
                 Product.quantity
                        │
                        ▼
            Current Inventory Snapshot

        Used By

        • Product Listing
        • Search Results
        • Product Details
        • Shopping Cart
        • Checkout Validation
        • Availability Check

        Query

        SELECT quantity
        FROM product
        WHERE id = ?

        Time Complexity

              O(1)
```

Instead of calculating stock by reading thousands of historical transactions every time, the application simply reads the latest quantity from the **Product** table.

---

```text
              StockTransaction
                     │
                     ▼
         Complete Inventory History

Used For

• Inventory Audit
• Purchase History
• Sales Reports
• Damage Tracking
• Return Tracking
• Warehouse Analytics
• Inventory Reconciliation
• Admin Dashboard

Example

+100   PURCHASE

-3     SALE

+2     RETURN

-5     DAMAGE

+50    PURCHASE

...
```

This table is never used to determine current stock during checkout.

Instead, it acts as the **bank statement** of your inventory.

---

# Bank Account Analogy

```text
             Product.quantity

        Today's Bank Balance

              ₹97


                    vs


         StockTransaction History

+1000 Salary

-150 Grocery

-200 Fuel

+500 Refund

-100 Electricity

...

Bank Statement
```

Exactly the same idea.

Current balance is stored separately from transaction history.

---

# InventoryService

Only one class in the entire application is allowed to modify inventory.

```text
                  OrderService
                        │
                        ▼
              InventoryService
                        │
        ┌───────────────┼────────────────┐
        │               │                │
        ▼               ▼                ▼

 Purchase Stock      Deduct Stock      Return Stock

        │               │                │

        └───────────────┼────────────────┘
                        │
                        ▼
               Database Transaction
```

Nobody else updates `Product.quantity`.

This guarantees consistency.

---

# Stock Deduction Flow

```text
Customer Orders

3 Apples

Current Stock

100

              │
              ▼

InventoryService.deductStock(productId,3)

              │
              ▼

BEGIN TRANSACTION

              │
              ▼

Update Product.quantity

100

↓

97

              │
              ▼

Insert StockTransaction

Type

SALE

Quantity

-3

              │
              ▼

COMMIT

              │
              ▼

Inventory Updated Successfully
```

Both updates happen inside the same database transaction.

Either both succeed or both fail.

---

# Inventory Operations

```text
                    InventoryService

                            │

        ┌───────────────────┼────────────────────┐

        ▼                   ▼                    ▼

Purchase Stock         Customer Sale        Customer Return

quantity += 100         quantity -= 2       quantity += 2

Insert PURCHASE         Insert SALE         Insert RETURN

                            │

                            ▼

                    Damage Stock

                    quantity -= 5

                    Insert DAMAGE
```

Every inventory movement creates a history record.

---

# Order Pricing Flow

```text
Customer Creates Order

            │

            ▼

Receive CreateOrderRequest

            │

            ▼

Fetch Product

            │

            ▼

Read Current Product Price

            │

            ▼

Apply Discount Rules

            │

            ▼

Calculate Tax

            │

            ▼

Calculate Final Line Total

            │

            ▼

Create OrderItem Snapshot

            │

            ▼

Repeat For Every Product

            │

            ▼

Calculate Order Totals

            │

            ▼

Create Order

            │

            ▼

Save Everything
```

All business logic lives inside **OrderService**.

Entities simply store values.

---

# Why OrderItem Stores Price

```text
Monday

Apple

Price

₹50

Customer Purchases

2 Apples

OrderItem

unit_price = ₹50

──────────────────────────────

Tuesday

Admin Changes Price

₹70

──────────────────────────────

Product

price = ₹70

──────────────────────────────

Old Order

Still Shows

₹50

Correct

New Orders

Show

₹70
```

Old invoices never change.

---

# OrderItem Snapshot

```text
                    OrderItem

┌──────────────────────────────────────────────┐

product_id

quantity

unit_price

discount_type

discount_value

tax_amount

line_total

created_at

└──────────────────────────────────────────────┘

Everything required to reproduce the invoice years later
is permanently stored.
```

---

# Order Totals

```text
                  Order

┌────────────────────────────────────────────┐

subtotal

discount

tax

shipping_charge

grand_total

payment_status

order_status

created_at

└────────────────────────────────────────────┘
```

Order stores totals.

OrderItem stores per-product pricing.

---

# Complete Production Order Flow

```text
                             Customer
                                 │
                                 ▼
                      Create Order Request
                                 │
                                 ▼
                         OrderController
                                 │
                                 ▼
                          OrderService
                                 │
     ┌───────────────────────────┼───────────────────────────┐
     │                           │                           │
     ▼                           ▼                           ▼

Read Product              Pricing Engine             InventoryService

(Current Price)      Discount + Tax Rules         Validate Stock

     │                           │                           │
     └───────────────┬───────────┴───────────────┬───────────┘
                     │                           │
                     ▼                           ▼
              Create OrderItem          Deduct Inventory
             (Historical Snapshot)      + Create Transaction
                     │                           │
                     └───────────────┬───────────┘
                                     │
                                     ▼
                             Calculate Totals
                                     │
                                     ▼
                               Create Order
                                     │
                                     ▼
                         Persist Database Records
                                     │
                                     ▼
                            Commit Transaction
                                     │
                                     ▼
                           Return Order Response
```

---

# Separation of Responsibilities

```text
                   Production E-Commerce Architecture

┌────────────────────────────────────────────────────────────────┐
│                          Product                               │
│                                                                │
│ • Current Selling Price                                        │
│ • Current Available Quantity                                   │
│ • Product Information                                          │
└────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────┐
│                        OrderService                            │
│                                                                │
│ • Validate Products                                             │
│ • Calculate Discounts                                           │
│ • Calculate Taxes                                                │
│ • Create OrderItems                                              │
│ • Calculate Order Totals                                         │
└────────────────────────────────────────────────────────────────┘
               │                              │
               ▼                              ▼
┌──────────────────────────┐      ┌──────────────────────────┐
│ InventoryService         │      │ OrderItem               │
│                          │      │                         │
│ • Update Stock           │      │ Historical Price        │
│ • Create Transactions    │      │ Historical Tax          │
│ • Validate Availability  │      │ Historical Discount     │
└──────────────────────────┘      │ Final Line Total        │
               │                  └──────────────────────────┘
               ▼
┌────────────────────────────────────────────────────────────────┐
│                    StockTransaction                            │
│                                                                │
│ • Purchase History                                              │
│ • Sale History                                                  │
│ • Return History                                                │
│ • Damage History                                                │
│ • Complete Audit Trail                                          │
└────────────────────────────────────────────────────────────────┘
```

This design provides **fast inventory lookups**, **accurate historical invoices**, **auditable inventory history**, **transactional consistency**, and a clear separation of responsibilities—making it the standard architecture used in mature production e-commerce systems.




# RAWW-

# ===========================================

# Inventory and Pricing Design Explained

A production-ready inventory and order management system typically stores data in **two places**:

1. **Product** – stores the current state of a product.
2. **StockTransaction** – stores the complete history of stock changes.

## Product Table

The `Product` table contains the latest information about a product.

| Field | Description |
|--------|-------------|
| `id` | Product ID |
| `name` | Product name |
| `price` | Current selling price |
| `quantity` | Current available stock |

Example:

```text
Product
-------
Apple
Price: ₹50
Quantity: 100
```

---

## StockTransaction Table

The `StockTransaction` table records **every stock movement**.

| Field | Description |
|--------|-------------|
| `id` | Transaction ID |
| `product_id` | Related product |
| `type` | Purchase, Sale, Return, Damage, etc. |
| `quantity_change` | Amount added or removed |

Example:

```text
Purchase  +100
Sale      -3
Return    +2
Damage    -5
```

---

# Why Store Both?

Although both tables contain stock-related information, they serve **different purposes**.

### Product

The `quantity` field in the `Product` table represents the **current stock available**.

Think of it like your **bank balance**.

```text
Product.quantity = Current Bank Balance
```

---

### StockTransaction

The `StockTransaction` table is like your **bank statement**.

It records **every transaction** that changed the stock.

```text
StockTransaction = Bank Statement
```

---

## Example

Initially:

```text
Product

Apple
Quantity = 100
```

Transaction history:

```text
Purchase +100
```

Now a customer buys **3 Apples**.

The `InventoryService` performs two operations inside a **single database transaction**.

### Step 1

Update the product quantity.

```text
100
↓
97
```

### Step 2

Insert a transaction.

```text
SALE
-3
```

Final result:

```text
Product.quantity = 97

History

+100
-3
```

Both updates happen in the **same database transaction**.

```text
BEGIN TRANSACTION

Update Product.quantity

Insert StockTransaction

COMMIT
```

This guarantees that the stock snapshot and transaction history always remain synchronized.

---

# Why Not Calculate Stock Every Time?

Technically, the current quantity can be calculated as:

```text
Current Quantity = SUM(All Stock Transactions)
```

For example:

```text
+100
-3
+2
-5
---------
94
```

However, calculating this every time a customer views a product would be slow.

Instead, the application stores the current quantity in the `Product` table and keeps it synchronized whenever stock changes.

Think of `Product.quantity` as a **cached value**.

This provides:

- Fast product pages
- Fast search results
- Fast checkout validation
- Constant-time stock lookup

Example query:

```sql
SELECT quantity
FROM product
WHERE id = ?;
```

This is much faster than scanning thousands of stock transactions.

---

# InventoryService

Only one class should be responsible for changing stock:

```text
InventoryService
```

For example:

```text
deductStock(productId, quantity)
```

Its workflow is:

```text
BEGIN TRANSACTION

Update Product.quantity

Insert StockTransaction

COMMIT
```

No other class should update `Product.quantity` directly.

This prevents inconsistent data and difficult-to-find bugs.

---

# Checking Stock Availability

Checking inventory becomes very simple.

```text
Product.quantity >= Requested Quantity
```

There is no need to scan the entire transaction history.

---

# Common Stock Operations

## Purchasing Inventory

Admin purchases 100 Apples.

```text
Product.quantity += 100

Insert Transaction

PURCHASE
+100
```

---

## Customer Purchase

Customer buys 2 Apples.

```text
Product.quantity -= 2

Insert Transaction

SALE
-2
```

---

## Product Return

Customer returns 2 Apples.

```text
Product.quantity += 2

Insert Transaction

RETURN
+2
```

---

## Damaged Stock

Five Apples become damaged.

```text
Product.quantity -= 5

Insert Transaction

DAMAGE
-5
```

Each stock movement updates both the current stock and the transaction history.

---

# Product Price

The `Product` table stores the **current selling price**.

Example:

```text
Apple

₹50
```

Tomorrow, the admin changes the price.

```text
₹55
```

Now every new customer sees:

```text
₹55
```

---

# Why OrderItem Stores Price

Suppose:

**Monday**

```text
Apple = ₹50
```

A customer places an order.

**Tuesday**

The admin changes the price.

```text
Apple = ₹70
```

If the order only stored:

```text
product_id
```

then every old order would incorrectly display:

```text
₹70
```

Instead, each `OrderItem` stores a **snapshot** of the price at the time of purchase.

```text
OrderItem

product_id
quantity
unit_price
```

Example:

```text
unit_price = ₹50
```

Even if the product later costs ₹70, the old order still correctly shows ₹50.

---

# Discounts

Discounts follow the same principle.

Suppose:

```text
Apple = ₹100
Festival Discount = 10%
```

The customer places an order.

The `OrderItem` stores:

```text
unit_price = 100
discount = 10%
```

After the festival ends, the discount is removed.

Old orders still retain the original discount that was applied.

---

# Taxes

Taxes should also be stored as a snapshot.

Do **not** recalculate old orders using today's tax rules.

Store the tax amount inside `OrderItem`.

Example:

```text
Price      = 100
Discount   = 10
Tax        = 18
Total      = 108
```

If GST changes in the future, historical orders remain accurate.

---

# Shipping Charges

Shipping belongs to the **Order**, not to individual `OrderItem`s.

Example:

```text
Subtotal         = 1000
Discount         = 50
Tax              = 180
Shipping Charge  = 100

Grand Total      = 1230
```

These values are calculated once for the entire order.

---

# Recommended Database Schema

## Product

```text
id
name
price
quantity
category_id
```

---

## Order

```text
id
subtotal
discount
tax
shipping_charge
grand_total
```

---

## OrderItem

```text
product_id
quantity
unit_price
discount_type
discount_value
tax_amount
line_total
```

Each `OrderItem` contains everything needed to recreate the invoice years later, even if product prices, taxes, or discounts have changed.

---

# Where Should Calculations Happen?

Business calculations should **not** be placed inside entities or controllers.

They belong in the `OrderService`.

Typical order flow:

```text
Receive CreateOrderRequest
        │
        ▼
Fetch Product
        │
        ▼
Read Product.price
        │
        ▼
Apply Discounts
        │
        ▼
Calculate Tax
        │
        ▼
Calculate Line Total
        │
        ▼
Create OrderItem
        │
        ▼
Sum All Line Totals
        │
        ▼
Create Order
```

The entities simply store the calculated values.

They should not contain pricing or business logic.

---

# Production Example

Suppose a customer orders:

```text
Apple

Quantity = 2

Product.price = ₹100
```

The `OrderService` calculates:

```text
Base Amount

2 × 100
= ₹200
```

```text
Discount

₹20
```

```text
Tax

₹32
```

```text
Line Total

₹212
```

The `OrderItem` stores:

```text
product_id      = Apple
quantity        = 2
unit_price      = 100
discount_value  = 20
tax_amount      = 32
line_total      = 212
```

The `Order` stores the aggregated totals:

```text
subtotal         = 200
discount         = 20
tax              = 32
shipping_charge  = 50
grand_total      = 262
```

---

# Summary

A well-designed e-commerce system separates responsibilities across different tables:

| Table | Responsibility |
|--------|----------------|
| **Product** | Stores the current catalog information (price, quantity, etc.). |
| **StockTransaction** | Stores the complete inventory history for auditing and reporting. |
| **OrderItem** | Stores historical snapshots of prices, discounts, taxes, and line totals. |
| **Order** | Stores order-level totals such as subtotal, tax, shipping, discount, and grand total. |

This architecture is commonly used in mature production e-commerce systems because it provides:

- Fast product lookups
- Efficient stock availability checks
- Accurate historical records
- Easy auditing and reporting
- Clean separation of responsibilities
- Better maintainability and scalability

