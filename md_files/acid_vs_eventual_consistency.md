# ACID vs. Eventual Consistency: A Project-Based Comparison

You asked for the difference between **ACID** and **Eventual Consistency**. The best way to understand this is to compare a **Monolith** (Old way) vs. your **Microservices** (Current way).

---

## 1. ACID (The Monolith)
**ACID** (Atomicity, Consistency, Isolation, Durability) is guaranteed by a single database (e.g., PostgreSQL).

### The Scenario: "Issue Drug to Patient"
Imagine all your code was in one `MonolithService`.

```java
@Transactional
public void issueDrug() {
    // 1. Save Patient Record
    patientRepo.save(record);
    
    // 2. Deduct Stock
    inventoryRepo.deductStock(itemId, 10);
    
    // THE MAGIC:
    // If line 2 fails, line 1 is AUTOMATICALLY undone (Rolled back).
}
```
*   **Result:** The data is **always** correct. It is impossible to have a Patient Record without the Stock being deducted.
*   **Consistency:** Immediate (Strong).
*   **Limitation:** It handles scale poorly. If the DB is locked, everyone waits.

---

## 2. Eventual Consistency (Your Microservices)
In your **Terminator** project, you split the DBs. `Orchestrator` has one DB, `Inventory` has another. **You cannot have ACID across two databases.**

### The Scenario: "Issue Drug to Patient" (Current Flow)

1.  **Orchestrator:** Creates `ReserveStockCommand` and saves to its DB. **(Commit 1)**
2.  **Orchestrator:** Sends message to Kafka.
3.  **... TIME GAP ...** (Milliseconds or Seconds)
4.  **Inventory:** Reads Kafka, updates Stock in its DB. **(Commit 2)**

### The "Eventual" Part
During that **Time Gap** (Step 3):
*   The User sees "Request Received".
*   BUT, the Inventory DB **still shows the old stock**.
*   The system is **temporarily inconsistent**.

### Why do we do this?
*   **Availability:** If `Inventory` is down, `Orchestrator` can still accept requests! The stock will be updated *eventually* when Inventory wakes up.
*   **Performance:** `Orchestrator` doesn't wait for the slow Inventory update. It fires and returns fast.

---

## Summary Table

| Feature | ACID (Monolith) | Eventual Consistency (Kafka/Saga) |
| :--- | :--- | :--- |
| **When is it consistent?** | Immediately (atomic) | After a short delay |
| **Failures** | Whole transaction fails | Parts can fail (requires Compensating/Rollback txn) |
| **Availability** | Lower (DB lock affects all) | Higher (Services are independent) |
| **Complexity** | Low (DB handles it) | High (You write Sagas & Listeners) |

**Your Project:** You are building an **Eventually Consistent** system using the **Saga Pattern**.
