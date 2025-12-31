# Saga Choreography Pattern

## Definition
**Saga Choreography** is a decentralized approach to managing distributed transactions (Sagas) in a microservices architecture. Instead of a central orchestrator telling each service what to do, services listen for **events** from other services and decide locally what action to take.

It requires communication via **events** (usually using a message broker like Kafka, RabbitMQ).
- **Service A** completes a task and publishes an event (e.g., `OrderCreated`).
- **Service B** listens for `OrderCreated`, performs its task, and publishes `PaymentProcessed`.
- **Service C** listens for `PaymentProcessed`, inventories the seat, and so on.

If a failure occurs (e.g., Payment Fails), the failing service publishes a failure event (e.g., `PaymentFailed`), and other services listen to it to trigger **Compensation Transactions** (undo changes).

---

## Example: Seat Booking System (MAMS)

Let's visualize a booking flow:

1.  **Booking Service**:
    - User places a booking.
    - Creates `Booking` in `PENDING` state.
    - Publishes **`BookingCreatedEvent`**.

2.  **Payment Service**:
    - Listens to `BookingCreatedEvent`.
    - Attempts to charge the user.
    - **Success**: Publishes **`PaymentSuccessEvent`**.
    - **Failure**: Publishes **`PaymentFailedEvent`**.

3.  **Seat Inventory Service**:
    - Listens to `PaymentSuccessEvent`.
    - Allocates the seat.
    - **Success**: Publishes **`InventoryReservedEvent`**.
    - **Failure**: Publishes **`InventoryReservationFailedEvent`**.

4.  **Booking Service (Completion/Compensation)**:
    - Listens to `InventoryReservedEvent` -> Updates Booking to **`CONFIRMED`**.
    - Listens to `PaymentFailedEvent` or `InventoryReservationFailedEvent` -> Updates Booking to **`CANCELLED`**.

**Compensation Flow**:
If **Seat Inventory** fails after payment was taken, it emits `InventoryReservationFailedEvent`.
- **Payment Service** listens to this and **Refunds** the customer.
- **Booking Service** listens to this and marks the booking as **`CANCELLED`**.

---

## Why is it Needed?
In microservices, you cannot use ACID transactions across multiple databases. Sagas utilize **Eventual Consistency**. Choreography is needed when you want to achieve this consistency without introducing a tight dependency on a central "God Service" (Orchestrator).

## Advantages & Benefits
1.  **Decentralized Control**: No single point of failure or bottleneck (like an orchestrator).
2.  **Loose Coupling**: Services only know about "Events", not about each other's APIs. `PaymentService` doesn't strictly need to know `InventoryService` exists, it just emits `PaymentSuccessEvent`.
3.  **Simplicity for Small Logic**: Very easy to implement for simple workflows (2-4 steps).
4.  **Agility**: Teams can modify their service's reaction to events without changing a central coordinator.

## Trade-offs (Disadvantages)
1.  **Complexity in Observability**: It is hard to see the "Global State" of a transaction. You verify the flow by tracing logs/events, not by querying one table.
2.  **Cyclic Dependencies**: Care is needed to avoid Service A reacting to Service B which reacts to Service A in a loop.
3.  **Integration Testing**: Testing the full interaction requires running all services and the broker.
4.  **Difficult Debugging**: If a transaction gets "stuck", it's harder to pinpoint which service missed an event compared to an Orchestrator which clearly shows "Waiting for Step X".

## Which Type of Projects Support It?
Saga Choreography is best suited for:
1.  **Event-Driven Architectures**: Projects already heavy on Kafka/RabbitMQ.
2.  **Smaller/Simpler Sagas**: Flows with fewer than 5-6 steps.
3.  **Independent Teams**: When teams managing services want full autonomy and don't want to rely on a shared Orchestrator team.
4.  **High Throughput Systems**: No central bottleneck means theoretically higher scalability.

**Not recommended for**: Very complex business flows with many conditional paths (use Orchestration instead).
