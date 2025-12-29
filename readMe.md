# Mastering Advanced Microservices (MAMS) - Saga Choreography

A multi-module Maven project demonstrating the **Saga Choreography Pattern** for distributed transactions using Spring Boot, Spring Cloud Stream, and Kafka.

## 🚀 Service Architecture & Ports

| Service | Port | Description |
| :--- | :--- | :--- |
| **API Gateway** | 8080 | Entry point for all requests |
| **Eureka Server** | 8761 | Service Discovery |
| **Config Server** | 8888 | Externalized Configuration |
| **Order Service** | 8082 | Orchestrates Saga; handles order lifecycle |
| **User Service** | 8081 | Manages user profiles and authentication |
| **Payment Service** | 8083 | Processes payments and handles refunds |
| **Inventory Service** | 8084 | Manages product stock and deductions |
| **Notification Service** | 8085 | Logs final Saga outcomes and alerts |

---

## 🔄 Saga Choreography Flow

The system uses event-driven choreography to maintain data consistency across microservices without a central orchestrator.

### 📉 Interaction Flow: Success Case (Payment & Inventory OK)

```text
[ Client ]
    |
    | 1. POST /orders
    v
[ Order Service ] (Status: ORDER_CREATED)
    |
    | 2. Publish OrderEvent (Topic: order-event)
    v
[ Payment Service ] (Status: PAYMENT_COMPLETED)
    |
    | 3. Publish PaymentEvent (Topic: payment-event) ----------+
    v                                                          |
[ Inventory Service ] (Status: INVENTORY_UPDATED)              | 4. Update Status
    |                                                          |
    | 5. Publish InventoryEvent (Topic: inventory-event)        |
    v                                                          v
[ Notification Service ] <--------------------------- [ Order Service ] (Status: ORDER_COMPLETED)
      (Logs Success)
```

**Step-by-Step Success Execution:**
1. **Client** sends an order request to **Order Service**.
2. **Order Service** creates an order in `ORDER_CREATED` state and emits an `OrderEvent`.
3. **Payment Service** consumes the event, processes payment, and emits a `PaymentEvent`.
4. **Inventory Service** consumes the payment success, deducts stock, and emits an `InventoryEvent`.
5. **Order Service** listens to all events and updates the final status to `ORDER_COMPLETED`.
6. **Notification Service** logs the final outcome.

### 🛑 Interaction Flow: Failure Case (Payment Failed)

```text
[ Client ]
    |
    | 1. POST /orders
    v
[ Order Service ] (Status: ORDER_CREATED)
    |
    | 2. Publish OrderEvent (Topic: order-event)
    v
[ Payment Service ] (Status: PAYMENT_FAILED)
    |
    | 3. Publish PaymentEvent (Topic: payment-event)
    v
[ Order Service ] (Status: ORDER_CANCELLED)
    |
    | 4. Log Failure Callback
    v
[ Notification Service ]
      (Logs Failure/Cancellation)
```

**Step-by-Step Failure Execution:**
1. **Client** sends an order request to **Order Service**.
2. **Order Service** emits an `OrderEvent` with status `ORDER_CREATED`.
3. **Payment Service** fails to process payment and emits a `PaymentEvent` with status `PAYMENT_FAILED`.
4. **Order Service** consumes the failure and rolls back by setting status to `ORDER_CANCELLED`.
5. **Notification Service** logs the cancellation for monitoring.

### 📉 Status Transitions
- **ORDER_CREATED**: Order received, awaiting payment.
- **PAYMENT_COMPLETED**: Funds authorized/captured.
- **PAYMENT_FAILED**: Transaction rejected (leads to `ORDER_CANCELLED`).
- **INVENTORY_UPDATED**: Stock deducted successfully (leads to `ORDER_COMPLETED`).
- **INVENTORY_FAILED**: Out of stock (leads to `ORDER_CANCELLED` and `PAYMENT_CANCELLED`).

---

## 🛠️ API Documentation

### **Order Service** (Port 8082)
`POST http://localhost:8082/orders` - Initiates a new order and starts the Saga flow.
```bash
curl -X POST http://localhost:8082/orders -H "Content-Type: application/json" -d "{\"userId\": 1, \"productId\": 101, \"amount\": 100}"
```

### **User Service** (Port 8081)
`POST http://localhost:8081/api/users` - Creates a new user profile.
```bash
curl -X POST http://localhost:8081/api/users -H "Content-Type: application/json" -d "{\"username\": \"john_doe\", \"email\": \"john@example.com\", \"password\": \"securepassword123\"}"
```

### **Payment Service** (Port 8083)
`GET http://localhost:8083/payment` - Retrieves all processed transactions.
```bash
curl -X GET http://localhost:8083/payment
```

### **Inventory Service** (Port 8084)
`GET http://localhost:8084/inventory` - Checks current stock levels for products.
```bash
curl -X GET http://localhost:8084/inventory
```

---

## 🧪 Troubleshooting

### Inventory Service Startup Error
If you encounter `BeanDefinitionStoreException` related to `ContextFunctionCatalogAutoConfiguration` in Spring Boot 3.5.3, add the following to your `application.yml`:
```yaml
spring:
  cloud:
    function:
      scan:
        enabled: false
```
*Note: This bypasses automatic function scanning which can conflict with manual bean definitions in newer Spring Boot versions.*

---

## 🏗️ Build & Run
1. Start Kafka/Zookeeper.
2. Build all modules: `mvn clean install -DskipTests`.

---

## 🛠️ Development Process (Step-by-Step)

Developing a Saga Choreography project follows a structured event-driven approach. Here is the process used to build this system:

### 1. Define Shared Data Models (`common-dtos`)
- Create a central module to store shared DTOs, Event classes, and Enums.
- Define `OrderStatus`, `PaymentStatus`, and `InventoryStatus`.
- Create `OrderEvent`, `PaymentEvent`, and `InventoryEvent` structures.

### 2. Configure Messaging Infrastructure
- Set up **Kafka** and **Spring Cloud Stream** in each microservice.
- Define function bindings in `application.yml` (e.g., `orderSupplier`, `paymentProcessor`).
- Use `spring-cloud-starter-stream-kafka` for high-throughput messaging.

### 3. Implement the Order Entry Point
- In `Order Service`, create a REST controller to receive order requests.
- Save the order to the database with a `ORDER_CREATED` status.
- Publish the initial `OrderEvent` to start the Saga.

### 4. Implement Reactive Consumers (Choreography)
- **Payment Service**: Listen to `order-event`. Deduct funds. Publish `PaymentEvent` (Success or Failure).
- **Inventory Service**: Listen to `payment-event` (Success). Deduct stock. Publish `InventoryEvent` (Success or Failure).
- **Notification Service**: Listen to all events to provide a holistic view/audit log.

### 5. Establish Compensation & Rollback Logic
- **Order Service Compensation**: Listen for `PAYMENT_FAILED` or `INVENTORY_FAILED` to transition the order to `ORDER_CANCELLED`.
- **Payment Service Compensation**: If inventory fails, the Payment Service must listen to `INVENTORY_FAILED` to initiate a refund.

### 6. Centralize Build & Standardize Ports
- Create a **Root POM** to manage all microservices as a multi-module project.
- Align dependency versions (e.g., Spring Boot 3.5.3 + Spring Cloud 2024.0.x).
- Standardize ports across services to avoid conflicts (8081, 8082, etc.).

### 7. Global Fixes & Troubleshooting
- Apply startup fixes like `spring.cloud.function.scan.enabled: false` to resolve modern Spring Boot compatibility issues.
- Enforce **Java 17** across the entire project for consistency.

