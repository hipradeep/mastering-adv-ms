# Kafka Offsets and Consumer Groups

You asked: *"how offset is increate and how message consume by consumer group"*

## 1. How the Offset "Increases"

In a database, you might increment a counter (`ID = ID + 1`). In Kafka, it works differently. The Offset is simply the **ID of the message** in the log (0, 1, 2...).

The "increase" happens in two ways:

### A. The Committed Offset (The important one)
The "Current Offset" is not stored in the broker's logic, but rather **managed by the Consumer Group**.
1.  **Read:** Your `inventory-service` reads Message #5.
2.  **Process:** It runs your code.
3.  **Commit:** When your function finishes successfully, the Spring library sends a signal to Kafka: *"Consumer Group 'inventory-group' has finished Message #5."*
4.  **Update:** Kafka updates the `__consumer_offsets` topic to say: `inventory-group -> Topic A -> Offset 6`.

**So, the offset "increases" because your consumer tells Kafka it successfully finished the previous work.**

### B. What if it crashes?
If your app crashes while processing Message #5 (before the commit), Kafka still thinks you are at Offset #5. When you restart, it gives you Message #5 again.

---

## 2. How a Consumer Group Consumes Messages

A **Consumer Group** is like a generic team of workers. In your code, this is defined by:
`groupId = "inventory-group"`

### The Golden Rule: 1 Partition = 1 Consumer
Kafka ensures that **only one consumer instance** in a group can read from a specific partition at a time. This guarantees ordering.

### scenario in Your Project
You have `num.partitions=1` configured in `server.properties`.

#### Scenario A: One Instance (Normal)
*   **Orchestrator** sends 100 messages.
*   **Inventory Inst 1** reads all 100 messages one by one.

#### Scenario B: Two Instances (Scale Up)
*   You start **Inventory Inst 1**. It grabs the lock on Partition 0.
*   You start **Inventory Inst 2**.
*   **Result:** **Instance 2 will sit IDLE.**
*   **Why?** There is only 1 partition. Kafka cannot split it. Instance 1 has the "exclusive lock" on that partition.

#### Scenario C: Scaling Correctly
To use 2 consumers, you must change `num.partitions=2` in `server.properties` (and restart/reset topics).
*   **Partition 0** -> Goes to **Instance 1**
*   **Partition 1** -> Goes to **Instance 2**
*   Now you have parallel processing.

### Visual Summary
```
[Topic A (Partition 0)]  <====>  [Inventory Service 1] (ACTIVE)
                                 
                                 [Inventory Service 2] (IDLE - Waiting for Inst 1 to die)
```
