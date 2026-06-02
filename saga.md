# The Saga Pattern in Microservices

The **Saga Pattern** is a design pattern used to manage data consistency across microservices in distributed transaction scenarios. A saga is a sequence of local transactions where each transaction updates data within a single service.

## 1. The Challenge in Microservices

In a monolithic application, you typically use a single relational database. Generally, you can use **ACID** (Atomicity, Consistency, Isolation, Durability) transactions to ensure data consistency. If one part of a transaction fails, the entire transaction rolls back.

In a **Microservices Architecture**:
- Each service has its own database (Database-per-service pattern).
- Business transactions often span multiple services.
- Local ACID transactions cannot span multiple databases.
- Traditional distributed transactions (like Two-Phase Commit or 2PC) are often too synchronous, locking resources and reducing availability and scalability (CAP Theorem limitations).

**Problem:** How do you ensure that a multi-step business process (like placing an order) is consistent? If the Order Service creates an order, but the Payment Service fails to charge the customer, how do you ensure the system doesn't end up in an invalid state?

## 2. Solution: The Saga Pattern

A Saga breaks a distributed transaction into a sequence of **local transactions**.

1.  **Local Transaction:** Each step in the business process is a local atomic transaction in one service.
2.  **Triggering Next Step:** When a local transaction completes, it publishes an event or message that triggers the next local transaction in the saga.
3.  **Compensation:** If a local transaction fails because it violates a business rule, the saga executes a series of **compensating transactions** that undo the changes that were made by the preceding local transactions.

### Key Concept: Compensation
Unlike ACID transactions which can simply "rollback", distributed transactions that have already committed their local changes cannot be technically rolled back. Instead, they must be **compensated**.

| Operation | Compensating Transaction (Undo) |
| :--- | :--- |
| `createOrder()` | `rejectOrder()` or `cancelOrder()` |
| `reserveCredit()` | `releaseCredit()` |
| `updateInventory()` | `revertInventory()` |

## 3. Implementation Approaches

There are two main ways to coordinate sagas: **Choreography** and **Orchestration**.

### A. Choreography (Events) - Decentralized
Each service involved in the saga listens for events from other services and decides if an action should be taken.

**Example Flow (Order -> Payment -> Inventory):**
1.  **Order Service**: Creates an `Order` in `PENDING` state and publishes an `OrderCreated` event.
2.  **Payment Service**: Listens for `OrderCreated`. Tries to deduct credit.
    - *Success*: Publishes `PaymentProcessed` event.
    - *Failure*: Publishes `PaymentFailed` event.
3.  **Inventory Service**: Listens for `PaymentProcessed`. Tries to reserve stock.
    - *Success*: Publishes `InventoryReserved` event.
    - *Failure*: Publishes `InventoryFailed` event.
4.  **Order Service**:
    - Listens for `InventoryReserved` -> Updates Order to `COMPLETED`.
    - Listens for `PaymentFailed` or `InventoryFailed` -> Updates Order to `CANCELLED`.

**Pros:**
- Simple to start with.
- Loose coupling (participants don't know about each other specifically).
- No central coordinator (Single Point of Failure).

**Cons:**
- Visualization is hard: Which service listens to what? Cyclic dependencies can occur.
- Integration testing is complex.
- Difficult to track the state of a complex saga.

### B. Orchestration (Command/Reply) - Centralized
A central **Orchestrator** (often the service triggering the process) tells the participants what local transactions to execute.

**Example Flow:**
1.  **Order Service (Orchestrator)**: Receives a request to place an order.
    - Creates an `Order` saga tracker.
2.  **Order Service**: Sends a `ProcessPayment` command to the **Payment Channel**.
3.  **Payment Service**: Processes payment and replies with `PaymentSuccess`.
4.  **Order Service**: Receives success, sends `ReserveInventory` command to **Inventory Channel**.
5.  **Inventory Service**: Reserves stock and replies with `InventorySuccess`.
6.  **Order Service**: Completes the order.

*If a step fails, the Orchestrator is responsible for sending the necessary rollback/compensation commands to the services that already completed their work.*

**Pros:**
- Centralized logic: Easy to understand the flow in one place.
- Decouples the participants from each other (they only know commands, not who sent them).
- Easier to manage cyclic dependencies.

**Cons:**
- The Orchestrator can become too smart/complex (Logic bottleneck).
- Additional infrastructure complexity if using a dedicated orchestration engine.

## 4. When to Use Which?

| Feature | Choreography | Orchestration |
| :--- | :--- | :--- |
| **Complexity** | Low (Simple flows) | High (Complex flows) |
| **Coupling** | Low (Event-based) | Moderate (Command-based) |
| **Participants** | Small number (2-4 services) | Large number (many services) |
| **Visibility** | Hard to track | Easy to track (Centralized) |

## Summary
The Saga pattern is essential for maintaining data consistency in microservices without sacrificing availability. It trades strict ACID atomicity for **Eventual Consistency**.

- **ACID**: All or nothing, immediately.
- **Saga**: Eventually consistent. If something fails mid-way, we undo (compensate) the done parts.
