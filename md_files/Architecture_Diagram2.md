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
    Inventory -.->|Broker Down| FailState[⚠️ Data Inconsistency<br/>Stock Reserved but Event Lost]
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
class FailState error;
classDef error fill:#ffcdd2,stroke:#c62828,stroke-width:2px,stroke-dasharray: 5 5;
class DB_Orch,DB_Inv,DB_Issue db;
class Kafka bus;

```

## Failure Scenario: Broker Down after Stock Reservation
If the **Message Broker (Kafka)** is down when the `StockReservedEvent` is fired:

1.  **State**: The `Inventory Service` has successfully committed the stock reservation to `Inventory DB`.
2.  **Failure**: The attempt to publish `StockReservedEvent` to Kafka fails (Connection Refused / Timeout).
3.  **Result**:
    *   The Java process catches the exception.
    *   The `ReserveStockCommand` message processing fails.
    *   **Risk**: If the consumer retries the message without idempotency checks, the stock might be deducted *again* (Double Booking).
    *   **Inconsistency**: The Orchestrator stays waiting for a response, while the stock is physically reserved.
