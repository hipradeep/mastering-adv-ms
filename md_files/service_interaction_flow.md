# Service-to-Service Interaction Flow: Issue Transaction Saga

This document outlines the step-by-step function calls and message flows between the `orchestrator-service`, `inventory-service`, and `issue-to-patient-service`.

## Overview
The flow implements a Saga pattern for distributing transactions across microservices.
**Entry Point:** `POST /api/orchestrator/issue` (Orchestrator Service)

---

## Detailed Step-by-Step Flow

### 1. Initiation (Orchestrator Service)
**Goal:** Receive request, generate Transaction ID, and start the Saga.

*   **File:** `orchestrator-service/.../controller/OrchestratorController.java`
    *   **Method:** `initiateIssueTransaction(@RequestBody IssueRequestDto requestDto)`
    *   **Action:** Validates input and calls `orchestratorService.initiateIssueTransaction(requestDto)`.

*   **File:** `orchestrator-service/.../service/OrchestratorService.java`
    *   **Method:** `initiateIssueTransaction(IssueRequestDto requestDto)`
    *   **Action:**
        1.  Generates a unique `transactionId`.
        2.  Saves the request state in memory (`OrchestratorKafkaConsumer.SAGA_STATE`).
        3.  Constructs a `ReserveStockCommand`.
        4.  **Publishes** `ReserveStockCommand` to Topic `inventory.commands`.

---

### 2. Stock Reservation (Inventory Service)
**Goal:** Reserve stock for the requested items.

*   **File:** `inventory-service/.../service/InventoryKafkaConsumer.java`
    *   **Method:** `consume(ConsumerRecord<?, ?> record)` -> `handleReserve(ReserveStockCommand command)`
    *   **Action:** Consumes `ReserveStockCommand` from `inventory.commands`.
    *   **Call:** Calls `inventoryService.updateStock(...)` for each item.

*   **File:** `inventory-service/.../service/InventoryService.java`
    *   **Method:** `updateStock(...)`
    *   **Action:** Updates the database to mock/perform the reservation.

*   **File:** `inventory-service/.../service/InventoryKafkaConsumer.java`
    *   **Method:** `handleReserve(...)` (continuation)
    *   **Action:**
        *   **If Success:** Constructs `StockReservedEvent` (success=true).
        *   **If Failed:** Constructs `StockReservedEvent` (success=false).
        *   **Publishes** `StockReservedEvent` to Topic `orchestrator.replies`.

---

### 3. Orchestrator Decision 1 (Orchestrator Service)
**Goal:** Handle reservation result and trigger Issue Creation.

*   **File:** `orchestrator-service/.../service/OrchestratorKafkaConsumer.java`
    *   **Method:** `consume(...)` -> `handleStockReservedBox(StockReservedEvent event)`
    *   **Action:** Consumes `StockReservedEvent` from `orchestrator.replies`.
    *   **Logic:**
        *   **If Success:**
            1.  Retrieves original request from `SAGA_STATE` using `transactionId`.
            2.  Constructs `CreateIssueCommand`.
            3.  **Publishes** `CreateIssueCommand` to Topic `issue.commands`.
        *   **If Failure:**
            1.  Aborts Saga.
            2.  Removes state from `SAGA_STATE`.

---

### 4. Issue Creation (Issue-to-Patient Service)
**Goal:** Create the issue record for the patient.

*   **File:** `issue-to-patient-service/.../service/IssueKafkaConsumer.java`
    *   **Method:** `consume(CreateIssueCommand command)`
    *   **Action:** Consumes `CreateIssueCommand` from `issue.commands`.
    *   **Call:** Calls `issueService.createIssue(requestDto)`.

*   **File:** `issue-to-patient-service/.../service/IssueToPatientService.java`
    *   **Method:** `createIssue(IssueRequestDto requestDto)`
    *   **Action:** Persists the issue details to the database.

*   **File:** `issue-to-patient-service/.../service/IssueKafkaConsumer.java`
    *   **Method:** `consume(...)` (continuation)
    *   **Action:**
        *   **If Success:** Creates `IssueCreatedEvent` (success=true) and **Publishes** to Topic `orchestrator.replies`.
        *   **If Failure:** Creates `IssueCreatedEvent` (success=false) and **Publishes** to Topic `orchestrator.replies`.

---

### 5. Orchestrator Decision 2 (Orchestrator Service)
**Goal:** Finalize transaction (Commit or Rollback).

*   **File:** `orchestrator-service/.../service/OrchestratorKafkaConsumer.java`
    *   **Method:** `consume(...)` -> `handleIssueCreatedBox(IssueCreatedEvent event)`
    *   **Action:** Consumes `IssueCreatedEvent` from `orchestrator.replies`.
    *   **Logic:**
        *   **If Success (Commit):**
            1.  Constructs `ConfirmStockCommand` with `commit=true`.
            2.  **Publishes** `ConfirmStockCommand` to Topic `inventory.commands`.
            3.  Removes state from `SAGA_STATE` (Saga Complete).
        *   **If Failure (Rollback):**
            1.  Constructs `ConfirmStockCommand` with `commit=false`.
            2.  **Publishes** `ConfirmStockCommand` to Topic `inventory.commands`.
            3.  Removes state from `SAGA_STATE`.

---

### 6. Final Confirmation (Inventory Service)
**Goal:** Acknowledge commit or handle rollback (logging for now).

*   **File:** `inventory-service/.../service/InventoryKafkaConsumer.java`
    *   **Method:** `consume(...)` -> `handleConfirm(ConfirmStockCommand command)`
    *   **Action:** Consumes `ConfirmStockCommand` from `inventory.commands`.
    *   **Logic:**
        *   Logs satisfaction/completion of the transaction.
        *   (Note: Rollback logic is currently a placeholder log).
