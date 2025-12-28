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
*   **Exporter**: `zipkin-reporter-brave`
*   **Propagation**: Trace IDs and Span IDs are automatically propagated across HTTP calls using `RestTemplate`.

### Tracing Flow
When you place an order:
1.  **Order Service** receives `POST /api/order`. A distinct **Trace ID** is generated.
2.  **Order Service** calls **Inventory Service** (`PUT /api/inventory/reduce`). The Trace ID is passed in headers.
3.  **Order Service** calls **Notification Service** (`POST /api/notification`). The same Trace ID is passed.
4.  **Zipkin** collects these spans and visually displays the entire request lifecycle.

### Setup & Verification
1.  **Start Zipkin**:
    *   Download `zipkin.jar` from [Zipkin Quickstart](https://zipkin.io/pages/quickstart.html).
    *   Run: `java -jar zipkin.jar`
2.  **Access UI**: `http://localhost:9411`
3.  **Logs**: Check console logs for `[service-name, traceId, spanId]`.

---

## Infrastructure Services

### 1. Eureka Discovery Server
**Base URL**: `http://localhost:8761`
*   **Role**: Service Registry. All microservices register themselves here.
*   **Dashboard**: Access the UI to see running service instances.

### 2. API Gateway
**Base URL**: `http://localhost:8080`
*   **Role**: Single Entry Point. Routes requests to appropriate microservices.
*   **Routing**:
    *   `/api/order/**` -> `order-service`
    *   `/api/inventory/**` -> `inventory-service`
    *   `/api/notification/**` -> `notification-service`
    *   `/api/users/**` -> `user-service`
*   **Usage**: Clients should primarily communicate with this service.

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

### Run Services (Automated)
This project includes a startup script `run_all.bat` for Windows.

**Usage:**
1.  Ensure `zipkin.jar` is in the project root.
2.  Run `run_all.bat`.

It will start:
1.  Zipkin (Manual JAR)
2.  Eureka Server
3.  API Gateway
4.  All Microservices

### Troubleshooting & Configuration Notes
1.  **Spring Cloud Compatibility**:
    *   Since we are using Spring Boot 3.5.3, we have explicitly disabled the compatibility verifier in all `application.yml` files:
        ```yaml
        spring.cloud.compatibility-verifier.enabled: false
        ```
2.  **Hostname Resolution (UnknownHostException)**:
    *   To fix DNS resolution issues on local Windows environments, all services are configured to register with Eureka using their IP address:
        ```yaml
        eureka.instance.prefer-ip-address: true
        ```
