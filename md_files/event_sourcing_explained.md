# Event Sourcing Explained

## Core Concept
In traditional **CRUD** (Create, Read, Update, Delete) systems, you store the **current state** of an object. If you update a user's address, the old address is overwritten and lost.

In **Event Sourcing**, you do not store the current state. Instead, you store a sequence of **events** (facts that happened in the past). The "current state" is derived by replaying these events from the beginning.

### The Golden Rule
> "The database of events is the single source of truth."

---

## How It Works (The Bank Example)

Imagine a bank account.

### Traditional Approach (State-Oriented)
**Table: `Accounts`**

| ID | Owner | Balance |
| :--- | :--- | :--- |
| 1 | Alice | $150 |

*Scenario:* If Alice deposits $50:
`UPDATE Accounts SET Balance = 200 WHERE ID = 1;`
*(The history of how she got to $200 is gone, unless you have a separate audit log).*

### Event Sourcing Approach
**Table: `Events`**

| EventID | StreamID (Account) | EventType | Data | Timestamp |
| :--- | :--- | :--- | :--- | :--- |
| 101 | 1 | AccountOpened | `{Owner: "Alice", Initial: $0}` | 10:00 AM |
| 102 | 1 | MoneyDeposited | `{Amount: $100}` | 10:05 AM |
| 103 | 1 | MoneyWithdrawn | `{Amount: $50}` | 11:00 AM |
| 104 | 1 | MoneyDeposited | `{Amount: $100}` | 12:00 PM |

**To get the Current Balance:**
You read all events for Account 1 and apply them:
1.  Start: $0
2.  +100 = $100
3.  -50 = $50
4.  +100 = **$150**

---

## Why Use It? (The Pros)

1.  **Complete Audit Trail:** You don't just know *what* the data is; you know *how* it got there.
2.  **Debugging & Corrections:** If you find a bug in your calculation logic, you can fix the logic and replay the original events to get the correct state.
3.  **Performance (Writes):** Appending to a log is extremely fast (no locks/updates).
4.  **Decoupling:** Services listen to events rather than querying shared databases.

---

## The Challenges (The Cons)

1.  **Complexity:** Querying by state (e.g., "Balances > $100") is hard because state doesn't exist in the DB.
    *   *Solution:* **CQRS** (Command Query Responsibility Segregation) - use a separate Read DB updated by events.
2.  **Event Schema Evolution:** Handling changes to event structures over time (versioning).
3.  **Storage Size:** The log grows forever.
    *   *Solution:* **Snapshots** (save state every N events to speed up replay).

---

## When to use it?
-   **Financial Systems:** Ledgers, Accounting.
-   **Complex Domains:** Logistics, Order Management.
-   **Collaborative Systems:** Document history tracks.

## When NOT to use it?
-   **Simple CRUD Apps:** Blogs, Dashboards.
