# Mastering Advanced Microservices

A multi-module Maven project demonstrating a microservices architecture with Spring Boot 3.x, featuring distributed tracing, service-to-service communication, and layered architecture.

## Modules
- **common-lib**: Shared utilities and DTOs.
- **gateway-service**: API Gateway.
- **config-server**: Centralized configuration server.
- **user-service**: User management.
- **inventory-service**: Inventory management (Stock check/update).
- **order-service**: Order processing (Calls Inventory & Notification).
- **notification-service**: Notification handling.

---

## Distributed Tracing Implementation

This project uses **Micrometer Tracing (Brave)** and **Zipkin** to trace requests across microservices.

### Implementation Details
*   **Framework**: Spring Boot 3.5.3
*   **Core Library**: `micrometer-tracing-bridge-brave`
*   **Reporting**: `zipkin-reporter-brave`
*   **Propagation**: Trace IDs and Span IDs are automatically propagated across HTTP calls using `RestTemplate`.

### Tracing Flow
When you place an order:
1.  **Order Service** receives `POST /api/order`. A distinct **Trace ID** is generated.
2.  **Order Service** calls **Inventory Service** (`PUT /api/inventory/reduce`). The Trace ID is passed in headers.
3.  **Order Service** calls **Notification Service** (`POST /api/notification`). The same Trace ID is passed.
4.  **Zipkin** collects these spans and visually displays the entire request lifecycle.

### Setup & Verification
1.  **Start Zipkin**: `docker run -d -p 9411:9411 openzipkin/zipkin`
2.  **Access UI**: `http://localhost:9411`
3.  **Logs**: Check console logs for `[service-name, traceId, spanId]`.

---

## API Documentation

### 1. User Service
**Base URL**: `http://localhost:8082`

| Method | Endpoint | Description | Body / Params |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/users` | Create User | `{"username": "john", "email": "john@example.com", "firstName": "John", "lastName": "Doe"}` |
| `GET` | `/api/users/{id}` | Get User | Path param: `id` |
| `GET` | `/api/users` | List Users | - |
| `GET` | `/api/users/trace` | Simulate Trace | - |

### 2. Inventory Service
**Base URL**: `http://localhost:8083` (Typically runs on 8083)

*Assumed Ports for local dev: Inventory: 8083, Order: 8081, Notification: 8084, User: 8082 (Adjust as per `application.yml`)*

| Method | Endpoint | Description | Body / Params |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/inventory/{sku-code}` | Check Stock | Path param: `sku-code` |
| `POST` | `/api/inventory` | Add/Update Stock | `{"skuCode": "iphone_13", "quantity": 100}` |
| `PUT` | `/api/inventory/reduce/{sku-code}` | Reduce Stock | Path: `sku-code`, Param: `quantity` |

### 3. Order Service
**Base URL**: `http://localhost:8081`

| Method | Endpoint | Description | Body / Params |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/order` | Place Order | `{"skuCode": "iphone_13", "price": 1200, "quantity": 1}` |

**Flow**: Checks/Reduces Inventory -> Saves Order -> Sends Notification.

### 4. Notification Service
**Base URL**: `http://localhost:8084`

| Method | Endpoint | Description | Body / Params |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/notification` | Send Notification | Body: `orderId` (Long) |

---

## Build & Run

### Prerequisites
*   Java 21
*   Maven 3.8+
*   Docker (for Zipkin, PostgreSQL)

### Run Services
```bash
# General command for each service
cd <service-folder>
mvn clean install
mvn spring-boot:run
```

**Recommended Order:**
1.  Config Server / Eureka (if used)
2.  Zipkin (`docker run ...`)
3.  Databases (PostgreSQL)
4.  Inventory, Notification, User Services
5.  Order Service
