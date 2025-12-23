# Mastering Advanced Microservices

A multi-module Maven project demonstrating microservices architecture with Spring Boot.

## Modules

| Module | Description | Port | URL |
|--------|-------------|------|-----|
| `common-lib` | Shared utilities and DTOs | - | - |
| `gateway-service` | API Gateway (Spring Cloud Gateway) | 8080 | http://localhost:8080 |
| `auth-server` | Authentication Server (OAuth2/OIDC) | 9000 | http://localhost:9000 |
| `config-server` | Centralized Configuration Server | 8888 | http://localhost:8888 |
| `eureka-server` | Service Registry (Eureka) | 8761 | http://localhost:8761 |
| `user-service` | User Management Service | 8082 | http://localhost:8082 |
| `order-service` | Order Management Service | 8083 | http://localhost:8083 |
| `inventory-service` | Inventory Management Service | 8084 | http://localhost:8084 |
| `notification-service` | Notification Service (Kafka Consumer) | 8085 | http://localhost:8085 |

## Features

### Rate Limiting
Implemented using **Resilience4j** in `user-service`.
- **Configuration**: 5 requests per minute.
- **Behavior**: After 5 requests within 60 seconds, subsequent requests are blocked (returns 500 or 429).
- **Target URL**: `http://localhost:8082/api/users/{id}`
- **Simulation**: Run `simulate_ratelimit.ps1` to verify.
