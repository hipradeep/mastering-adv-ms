# Mastering Advanced Microservices

A multi-module Maven project demonstrating a microservices architecture with advanced Kafka patterns.

## Modules
- **user-service**: User management (Kafka Producer).
- **order-service**: Order management (Kafka Consumer - Partition 0).
- **notification-service**: Notification management (Kafka Consumer - Partition 1).


---

## Kafka Architecture & Partition Flow

This project implements a partitioned event flow where specific services listen to specific partitions of the same topic.

### Topics
All topics are configured with **3 partitions** and `v2` suffix to ensure fresh creation.
- `user-events-v2`
- `order-events-v2`
- `notification-events-v2`

### Event Flow
The `user-service` publishes events to `user-events-v2`. The partition is determined by the client request.

1.  **Partition 0 Flow (Order Processing)**
    *   **Request:** `POST /user/create?partition=0`
    *   **Producer:** Sends message to `user-events-v2` (Partition 0).
    *   **Consumer:** `order-service` listens ONLY to Partition 0.
    *   **Result:** `order-service` processes the event; `notification-service` ignores it.

2.  **Partition 1 Flow (Notifications)**
    *   **Request:** `POST /user/create?partition=1`
    *   **Producer:** Sends message to `user-events-v2` (Partition 1).
    *   **Consumer:** `notification-service` listens ONLY to Partition 1.
    *   **Result:** `notification-service` processes the event; `order-service` ignores it.

### Configuration
- **User Service:** Producer. Can send to specific partitions.
- **Order Service:** Consumer. `@KafkaListener(topicPartitions = @TopicPartition(topic = "user-events-v2", partitions = "0"))`
- **Notification Service:** Consumer. `@KafkaListener(topicPartitions = @TopicPartition(topic = "user-events-v2", partitions = "1"))`

---

## Troubleshooting: Recent Issues

### Kafka Partition Assignment Mismatch
**Problem:** Messages intended for Partition 0 were landing in Partition 1 (or random partitions).
**Cause:** The `partition` parameter was sent in the JSON Body (e.g., `{"partition": 0}`), but the Controller expected a Query Parameter. This resulted in `null` being passed to the Kafka Producer, triggering the default "Sticky Partitioner".
**Solution:** Always pass the partition as a query parameter.
*   **Correct:** `http://localhost:8082/user/create?partition=0`
*   **Incorrect:** `http://localhost:8082/user/create` (with partition in body)

