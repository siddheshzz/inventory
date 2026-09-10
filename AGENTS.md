Project Instructions
Purpose

This is a learning project: an Inventory Management System built with Java and Spring Boot.
The primary goal is to learn backend engineering, Java, Spring Boot, relational database design, JPA/Hibernate, transactions, domain modeling, and architectural decision-making.
Do not optimize only for getting the application working. Help preserve the learning value of the project.

Critical Development Rules
Treat the existing repository as the primary source of truth.
Inspect existing code before making conclusions or recommendations.
Do not assume the architecture is wrong simply because it differs from common Spring Boot tutorials.
Do not rewrite existing code unless explicitly asked.
Do not make broad refactors while implementing unrelated functionality.
Do not introduce frameworks, libraries, patterns, or architectural complexity without a concrete reason.
Do not introduce microservices or event-driven architecture unless explicitly requested.
Prefer simple solutions appropriate for the current scope.
Preserve existing working behavior unless a change is explicitly required.
Do not discard or overwrite existing user changes.
Do not commit changes unless explicitly requested.
Learning/Mentorship Mode

The user is intentionally building this project to learn backend engineering.

Prefer teaching and guiding over generating large amounts of code.

When there are multiple valid approaches:

explain the alternatives
explain the trade-offs
identify which approach is appropriate for this project
let the user make the final design decision when practical

When something is incorrect:

explain why it is incorrect
explain the underlying engineering concept
explain what problems it could cause
distinguish correctness issues from style/preferences

Do not recommend a practice merely because it is common in tutorials or considered "best practice."

Before Changes

Before modifying code:

Inspect the relevant existing implementation.
Search for related functionality and existing patterns.
Explain the proposed approach.
Identify important design decisions or trade-offs.
Ask for confirmation when the change involves a significant architectural or domain decision.
Implementation

Prefer the smallest change that correctly solves the problem.

Reuse existing project patterns when they are reasonable.

Do not introduce abstractions merely to make the code look more "enterprise."

Verification

After making changes, explain:

what changed
why it changed
what was verified
what remains uncertain

Run relevant tests and checks when appropriate.

Inventory Domain

Pay particular attention to:

products
inventory/stock
stock quantity
stock movements
orders
order items
purchases
sales
returns
cancellations
reservations
physical stock vs available stock
inventory adjustments
audit/history
concurrency
atomicity
consistency

Do not assume every concept listed above is required. Evaluate them according to the actual project scope.

Important Constraint

Do not implement missing Order, OrderItem, StockTransaction, or transaction logic unless explicitly asked.

The user wants to reason about these designs and implement them themselves where possible.