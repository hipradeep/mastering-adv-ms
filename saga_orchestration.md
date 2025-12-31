# Saga Orchestration Pattern

## What is Saga Orchestration?
Saga Orchestration is a pattern for managing data consistency across microservices in distributed transactions. In this pattern, a central coordinator Service (the **Orchestrator**) tells the participants what local transactions to execute. The orchestrator handles all the decision-making and sequencing of business logic.

If a local transaction fails, the orchestrator invokes the necessary **Compensating Transactions** (rollbacks) on the participants to undo the changes made by the preceding successful steps.

![Saga Orchestration](images/Saga%20Orchestration%20in%20Microservices%20Explained%20with%20Real%20Movie%20Booking%20Example.png)

## Example: Movie Booking System
In our `mams` project, we implemented this flow:
1.  **Start**: User requests a booking. The `Booking Orchestrator` Service receives this request.
2.  **Step 1 (Inventory)**: Orchestrator sends a command to `Seat Inventory Service` to reserve seats.
    - *Success*: Orchestrator proceeds to payment.
    - *Failure*: Orchestrator marks the booking as failed immediately.
3.  **Step 2 (Payment)**: Orchestrator sends a command to `Payment Service` to process payment.
    - *Success*: Orchestrator confirms the booking.
    - *Failure*: Orchestrator triggers a **Compensation** step: it sends a command back to `Seat Inventory Service` to **Release** the previously reserved seats.

## Why is it Needed?
In a microservices architecture, each service has its own database. Traditional ACID transactions (like 2PC) are not feasible because they lock resources across distributed systems, reducing performance and availability. The Saga pattern provides a mechanism to ensure eventual consistency without heavy locking.

## Advantages & Benefits
1.  **Centralized Logic**: The complex logic of the workflow is in one place (the Orchestrator), making it easier to understand and manage than Choreography.
2.  **Decoupling**: Participant services (Inventory, Payment) don't need to know about each other. They only rely on comments from the Orchestrator.
3.  **Cyclic Dependencies**: Avoids cyclic dependencies between services which can occur in Choreography.
4.  **Monitoring**: Easy to track the status of the transaction since the Orchestrator knows the current state.

## Trade-offs
1.  **Single Point of Failure**: The Orchestrator becomes a critical component. If it goes down, new transactions cannot be processed (though Kafka ensures resilience).
2.  **Complexity**: Implementing an Orchestrator adds an extra service and complexity to the infrastructure.
3.  **Latency**: Additional network hops (Orchestrator -> Service -> Orchestrator) compared to direct service-to-service events in Choreography.

## What Type of Projects Support It?
Saga Orchestration is best suited for:
*   **Complex Workflows**: Transactions involving many steps (4+) or participants.
*   **High Control Requirements**: Systems where you need strict control over the sequence of execution.
*   **Greenfield Projects**: where you can design the orchestrator from the start.
*   **Compliance/Auditing**: Where you need a clear log of exactly what happened in a transaction logic at a central point.
