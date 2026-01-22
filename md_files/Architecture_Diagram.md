```mermaid
graph TD
%% Client
    subgraph Client Layer
        Browser[React Frontend]
    end

%% Orchestration
    subgraph Orchestration Layer
        Orchestrator[Orchestrator Service]
    end

%% Messaging
    subgraph Messaging Layer
        Kafka[(Apache Kafka)]
    end

%% Services
    subgraph Service Layer
        Inventory[Inventory Service]
        Issue[Issue Service]
    end

%% Data
    subgraph Data Layer
        DB_Orch[(Orchestrator DB)]
        DB_Inv[(Inventory DB)]
        DB_Issue[(Issue DB)]
    end

%% Client Flow
    Browser -->|HTTP POST /issue| Orchestrator

%% Orchestrator Commands
    Orchestrator -->|ReserveStockCommand| Kafka
    Orchestrator -->|Persist Saga State| DB_Orch

%% Inventory Processing
    Kafka -->|ReserveStockCommand| Inventory
    Inventory -->|Atomic Native SQL Update| DB_Inv
    Inventory -->|StockReservedEvent| Kafka
    Inventory -->|StockReservationFailedEvent| Kafka

%% Orchestrator Event Handling
    Kafka -->|StockReservedEvent| Orchestrator
    Kafka -->|StockReservationFailedEvent| Orchestrator

%% Issue Commands
    Orchestrator -->|CreateIssueCommand| Kafka

%% Issue Processing
    Kafka -->|CreateIssueCommand| Issue
    Issue -->|Save Issue| DB_Issue
    Issue -->|IssueCreatedEvent| Kafka
    Issue -->|IssueFailedEvent| Kafka

%% Final Saga Events
    Kafka -->|IssueCreatedEvent| Orchestrator
    Kafka -->|IssueFailedEvent| Orchestrator

%% Styling
    classDef service fill:#e3f2fd,stroke:#1e88e5,stroke-width:2px;
    classDef db fill:#fff3e0,stroke:#fb8c00,stroke-width:2px;
    classDef bus fill:#ede7f6,stroke:#5e35b1,stroke-width:2px;

    class Orchestrator,Inventory,Issue service;
    class DB_Orch,DB_Inv,DB_Issue db;
    class Kafka bus;

```

## Failure Scenarios: Broker Down (Service → Kafka)

### 1. Inventory Service → X Kafka
*   **State**: The `Inventory Service` has successfully committed the stock reservation to `Inventory DB`.
*   **Failure**: The attempt to publish `StockReservedEvent` to Kafka fails (Connection Refused / Timeout).
    *   The Java process catches the exception.
    *   The `ReserveStockCommand` message processing fails.

#### Case A: Actions "Retry within Timeout" (Transient Glitch)
*   **Scenario**: The Broker connection is unstable or blips for a few seconds.
*   **Action**: If the connection restores within the timeout, the `StockReservedEvent` is successfully sent.
*   **Result**: The Consumer finishes execution and **commits the offset**.

#### Case B: Actions "After Timeout" (Broker Down / Persistent Failure)
*   **Scenario**: The Broker remains down longer than the Producer's timeout config.
*   **Action**: The Consumer **THROWS this exception** back to the container. **DO NOT swallow it.**
*   **Result**:
    *   Kafka **does NOT commit the offset** for the `ReserveStockCommand`.
    *   **Redelivery**: When the Broker comes back up, Kafka **Redelivers** the command.
    *   **Idempotency**: The Idempotency logic detects the duplicate `TransactionID` and skips the DB update, only retrying the Event Publish.

---

### 2. Issue Service → X Kafka
*   **State**: The `Issue Service` has successfully saved the issue details to `Issue DB`.
*   **Failure**: The attempt to publish `IssueCreatedEvent` to Kafka fails (Connection Refused / Timeout).
    *   The Java process catches the exception.
    *   The `CreateIssueCommand` message processing fails.

#### Case A: Actions "Retry within Timeout" (Transient Glitch)
*   **Scenario**: The Broker connection is unstable or blips for a few seconds.
*   **Action**: If the connection restores within the timeout, the `IssueCreatedEvent` is successfully sent.
*   **Result**: The Consumer finishes execution and **commits the offset**.

#### Case B: Actions "After Timeout" (Broker Down / Persistent Failure)
*   **Scenario**: The Broker remains down longer than the Producer's timeout config.
*   **Action**: The Consumer **THROWS this exception** back to the container. **DO NOT swallow it.**
*   **Result**:
    *   Kafka **does NOT commit the offset** for the `CreateIssueCommand`.
    *   **Redelivery**: When the Broker comes back up, Kafka **Redelivers** the command.
    *   **Idempotency**: The Idempotency logic detects the duplicate `TransactionID` (or `IssueID`) and skips the DB update, only retrying the Event Publish.
