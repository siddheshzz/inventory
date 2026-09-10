I am building an Inventory Management System as a learning project to learn Java backend development with Spring Boot.

This is an important constraint: I built the project mostly using my own understanding and knowledge rather than following a tutorial or copying an existing architecture. I want to use this project to actually understand backend engineering, architecture, design decisions, and trade-offs.

The project is currently incomplete. Some major components are still remaining, especially:

Order
OrderItem
StockTransaction
Transaction-related logic
Possibly other supporting business logic around inventory/order processing

I want you to act as a senior backend engineer/architect with decades of experience in Java, Spring Boot, relational databases, enterprise backend systems, and production software development.

Your job is NOT to immediately rewrite my project or generate a bunch of code.

First, inspect and understand the existing codebase thoroughly.

Phase 1 — Understand the project

Start by exploring the repository.

Look at:

Project/package structure
Entities/models
Repositories/DAOs
Services
Controllers
DTOs
Mappers
Exception handling
Validation
Configuration
Database/schema/migrations
Relationships between entities
Transactions
Business logic
Naming conventions
Dependency direction
Tests, if present
Any other relevant code

Before giving recommendations, build a mental model of how the application currently works.

Do not assume that my architecture is correct or incorrect simply because it differs from common Spring Boot tutorials.

Try to understand why the current design might have been created this way.

Phase 2 — Review me like a senior developer

After understanding the project, give me a detailed code/architecture review.

I want honest feedback.

Do NOT be overly positive just to encourage me.

Tell me:

What I am doing well
What is acceptable for a learning project
What is genuinely good engineering
What is questionable
What is incorrect or likely to cause bugs
What will become problematic as the application grows
What would concern you in a professional code review
What concepts I appear to understand well
What backend concepts I appear to misunderstand
What important concepts I haven't encountered yet

Please distinguish between:

Bug / correctness issue
Design problem
Maintainability concern
Scalability concern
Production concern
Style/preference
Something that is completely fine for this learning project

Do not tell me to change something merely because "this is how Spring projects usually do it."

Explain the actual engineering reason behind each recommendation.

Phase 3 — Evaluate my Java/Spring knowledge

Based only on the code you see, estimate my current understanding of:

Core Java
OOP
Interfaces/abstraction
Collections
Exceptions
Generics
Streams/lambdas
JPA/Hibernate
Entity relationships
Database design
Transactions
Spring dependency injection
Spring MVC
REST API design
DTOs
Validation
Service-layer design
Repository design
Separation of concerns
Domain/business logic
Concurrency
Testing
Clean code
Architecture

Don't just give me a score.

For each area, explain what in my code led you to that conclusion.

Phase 4 — Pay special attention to Inventory Management domain design

Because this is an inventory system, I want you to specifically examine the domain model.

Think carefully about concepts such as:

Product
Inventory/stock
Stock quantity
Stock movement
StockTransaction
Order
OrderItem
Purchase/sale/return/cancellation
Stock reservation
Available stock vs physical stock
Inventory adjustments
Audit/history
Concurrency
Atomicity
Consistency
Database transactions

I don't necessarily expect all of these to exist yet.

Tell me which concepts are actually necessary for the scope of my project and which would be unnecessary overengineering.

Phase 5 — Help me design the remaining parts

Only AFTER reviewing the existing implementation, help me think through the missing:

Order
OrderItem
StockTransaction
Transaction/business logic

Do not immediately provide the final implementation.

Instead, guide me through the design.

For example, ask and discuss questions such as:

What should an Order represent?
What should an OrderItem represent?
Where should stock be reduced?
When should stock movement be recorded?
What should happen when an order is cancelled?
Should OrderItem contain a snapshot of product price?
Should stock quantity be stored directly or derived?
What exactly should StockTransaction represent?
Which operations should be atomic?
Where should @Transactional be used and why?
What happens if two users try to purchase the last item simultaneously?
What should happen if part of an operation fails?
Which invariants must always be maintained?

I want to reason about these decisions rather than blindly copy your solution.

VERY IMPORTANT — Teaching approach

Treat this as a mentorship/code-review session, not a code-generation task.

I am trying to learn backend engineering by building this project myself.

Therefore:

Do not rewrite large portions of the project unless I explicitly ask.
Do not introduce frameworks/libraries unnecessarily.
Do not over-engineer the application.
Do not introduce microservices.
Do not introduce event-driven architecture just because it is "enterprise."
Do not introduce design patterns unless there is an actual problem they solve here.
Prefer simple solutions that are appropriate for the current project.
Explain trade-offs.
Tell me when my existing approach is perfectly reasonable.
Challenge my assumptions when necessary.
If something is wrong, explain WHY it is wrong.
If there are multiple valid approaches, show me the alternatives and trade-offs.
Prefer asking me to implement something myself and then reviewing my implementation rather than writing everything for me.
IMPORTANT: Don't assume best practices blindly

I want to understand the difference between:

"industry best practice"

and

"appropriate engineering decision for this particular project."

For every major recommendation, explain:

What problem it solves
What happens if I don't do it
Why it matters here
Whether I actually need it now
Whether it can be introduced later
Code review format

After inspecting the repository, structure your first review roughly like this:

1. Overall assessment

Give me your honest assessment of the project as if you were reviewing a junior developer's backend project.

2. Architecture

Explain what architecture you see and whether it makes sense.

3. What's good

Specific things I did well, with references to the code.

4. Problems / risks

Rank them:

Critical
High
Medium
Low
Optional

Explain the reasoning behind each.

5. Domain model

Explain whether the current domain model makes sense for an inventory system.

6. Java/Spring assessment

Tell me what my code says about my current understanding.

7. Database/JPA assessment

Review relationships, constraints, fetching, cascading, IDs, persistence behavior, etc.

8. Transaction/concurrency assessment

This is particularly important for an inventory system.

Explain what I need to understand before implementing Order and StockTransaction.

9. Missing concepts

Tell me what I should learn next.

Separate:

Must learn now
Should learn soon
Can learn later
10. Recommended implementation roadmap

Give me a step-by-step order for implementing the remaining functionality.

The roadmap should minimize unnecessary rework.

11. Learning roadmap

Based on weaknesses you identify in my code, recommend the Java/Spring/backend concepts I should study next.

Do not give me a generic "learn Spring Boot" roadmap.

Make it specific to what you observed in my project.

Most important rule

Inspect the actual repository before giving me conclusions.

Do not give me generic advice based only on the description above.

Once you've inspected it, tell me:

"Here is how I currently understand your system..."

and describe the architecture/domain flow back to me.

I will then confirm whether your understanding is correct before we proceed with deeper design feedback.

Remember: my goal is not merely to make this project work.

My goal is to become a strong backend engineer by understanding why the system should be designed a certain way.

Act as my senior engineer/mentor throughout this process.