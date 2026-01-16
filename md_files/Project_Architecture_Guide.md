# Project Architecture & Best Practices Guide

## 1. Saga Transaction Status Tracking

### The Challenge
We need to track the status of the "Issue to Patient" background process (Saga) so the frontend can show a loader until completion (Success/Failure).

### Available Options
| Option | Technique | Pros | Cons |
| :--- | :--- | :--- | :--- |
| **1. In-Memory** | `ConcurrentHashMap` | Fast, Zero Setup | **Data Loss** on restart. Not scalable. |
| **2. WebSocket** | Push Notifications | Real-time | Complex infrastructure/firewall issues. |
| **3. Database (JPA)** | `SagaTransaction` Table | Persistent, Auditable, Standard | Requires DB Table & Repository. |

### Decision: Database Persistence (with Polling)
We chose **Option 3 (Database)** because it meets enterprise standards for reliability.

**Implementation Plan:**
1.  **Orchestrator Service (Owner)**: This table belongs **ONLY** to the Orchestrator. No other service (Inventory/Issue) will access it.
2.  **Entity**: Create `SagaTransaction` (ID, Status, Timestamp).
3.  **Flow**:
    *   *Start*: Insert `STARTED`.
    *   *Kafka Event*: Update to `SUCCESS` or `FAILURE`.
4.  **Frontend**: Poll `/status/{id}` every 2 seconds.

---

## 2. Concurrency Control (Inventory Updates)

### The Challenge
Prevent "Race Conditions" where two requests simultaneously deduct the same stock batch, resulting in incorrect inventory (e.g., both read 10, both deduct 5, both save 5, result is 5 instead of 0).

### Best Practice: Optimistic Locking
We avoid "Pessimistic Locking" (Database Row Locks) which performance bottlenecks. Instead, we use **Versioning**.

**How it works (`@Version`):**
1.  Add a `version` column to `HsttDrugCurrstockDtl`.
2.  **Read**: App reads `Item A (Version 1)`.
3.  **Write**: App sends update `SET qty=5, version=2 WHERE id=A AND version=1`.
4.  **Conflict**: If another user already updated it, the DB sees `version` is now `2`. The condition `version=1` fails. Update count is 0.
5.  **Result**: App throws `ObjectOptimisticLockingFailureException`. We catch this and tell the user to retry.

4.  **Conflict**: If another user already updated it, the DB sees `version` is now `2`. The condition `version=1` fails. Update count is 0.
5.  **Result**: App throws `ObjectOptimisticLockingFailureException`. We catch this and tell the user to retry.

### The Correct "JPA Way" (Recommended)
Instead of writing SQL queries, use Java objects. JPA handles the locking automatically.
```java
// 1. Fetch the object
HsttDrugCurrstockDtl stock = repository.findByStoreIdAndItemIdAndBatchNo(storeId, itemId, batchNo);

// 2. Modify the field (Java)
stock.setHstnumInhandQty(stock.getHstnumInhandQty() - quantity);

// 3. Save (JPA generates the safe SQL with locking)
repository.save(stock);
```

### WARNING: Native Queries
If you use `@Query(nativeQuery = true)`, you **BYPASS** JPA's Optimistic Locking. This leads to **Lost Updates**.
**Bad:** `UPDATE stock SET qty = :newQty ...` (Overwrites other transactions).

**Correct Native Approach 1 (Safe Calculation - Preferred):**
To avoid race conditions, let the Database do the calculation atommically:
```java
@Modifying
@Query(value = "UPDATE dwh.hstt_drug_currstock_dtl SET hstnum_inhand_qty = hstnum_inhand_qty - :deductQty WHERE hstnum_store_id = :storeId AND hstnum_itembrand_id = :itemBrandId AND hststr_batch_no = :batchNo AND hstnum_inhand_qty >= :deductQty", nativeQuery = true)
int deductStock(@Param("storeId") Integer storeId, @Param("itemBrandId") Integer itemBrandId, @Param("batchNo") String batchNo, @Param("deductQty") Integer deductQty);
```
*   **Result**: Returns 1 if successful, 0 if stock was insufficient.

**Correct Native Approach 2 (Manual Versioning - Alternative):**
If you need to update fields without calculation (e.g. changing a name), you MUST check version manually:
```java
@Modifying
@Query(value = "UPDATE dwh.hstt_drug_currstock_dtl SET hstnum_inhand_qty = :newQty, version = version + 1 WHERE hstnum_store_id = :storeId ... AND version = :oldVersion", nativeQuery = true)
int updateStock(..., @Param("oldVersion") Long oldVersion);
```
*   **Result**: Returns 0 if someone else updated the record first.

---

## 3. Unique ID Generation

### The Challenge
Generating unique, formatted IDs (e.g., `ITEM-2024-005`) safely in a high-concurrency environment without duplicates.

### Best Practice: DB Sequence + Custom Generator
We do **not** use the table's "Max ID + 1" (unsafe). We use Database Sequences.

**Implementation Plan:**
1.  **Database**: `CREATE SEQUENCE item_id_seq`. (Guarantees atomic uniqueness).
2.  **Java/Hibernate**: Create a custom `IdentifierGenerator`.
    *   Fetch `nextval` from Sequence -> `1005`.
    *   Format it -> `ITEM-2024-1005`.
3.  **Entity**: Annotate `@GeneratedValue(generator = "custom_id")`.

---

## Summary of Decisions
1.  **Architecture**: Upgrade Orchestrator to use **PostgreSQL**.
2.  **Concurrency**: Implement **Optimistic Locking** on Inventory.
3.  **IDs**: Use **Sequences** for generation.
