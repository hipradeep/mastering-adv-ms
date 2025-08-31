# Mastering Advanced Microservices

A monorepo containing multiple independent Spring Boot microservices demonstrating microservices architecture. Each service is standalone and managed in a single repository.

## Modules / Services

- `auth-server` – Handles authentication, JWT token generation, and validation.
- `config-server` – Central configuration server for all services.
- `eureka-server` – Service registry for service discovery.
- `gateway-service` – API Gateway to route requests to microservices.
- `inventory-service` – Manages product inventory and stock levels.
- `notification-service` – Sends notifications via email/SMS or other channels.
- `order-service` – Handles order creation, tracking, and management.
- `user-service` – Manages user data, authentication, and profiles.

---
## Branch & Feature Addition Guidelines

**Branch Name**: `feature/gateway`
- Implementation **Configure Spring Cloud Gateway**.

---
# Spring Security Microservices Architecture

## 🏗️ Architecture Overview
Two-tier security system with API Gateway handling authentication and downstream services handling authorization.

## 📁 Project Structure
```
gateway-service/
├── config/
│       ├── SecurityConfig.java
└── filter/
        ├── JwtReactiveAuthenticationManager.java

user-service/
├── config/
│       └── DownstreamSecurityConfig.java
└── filter/
        └── GatewayHeaderAuthFilter.java
```
## 🔐 Gateway Service Responsibilities

### Configuration
- **SecurityConfig.java** - Main security configuration with JWT setup

### Core Components
- **JwtReactiveAuthenticationManager** - Validates JWT tokens, 

### Key Features
- JWT token validation
- Role extraction from tokens
- Request routing with security headers
- Open endpoints configuration

## 🔒 Downstream Service Responsibilities

### Configuration
- **DownstreamSecurityConfig** - Security setup for downstream services

### Core Components
- **GatewayHeaderAuthFilter** - Validates gateway headers and sets security context

### Key Features
- Header-based authentication trust
- Method-level security with `@PreAuthorize`
- Business logic authorization

## 🛡️ Security Flow

### Authentication Flow
1. Client → Gateway with JWT
2. Gateway validates JWT → extracts roles
3. Gateway forwards request with headers:
    - `X-Username`: Authenticated username
    - `X-Roles`: Comma-separated roles
    - `X-Source`: "gateway" identifier

### Authorization Flow
1. Downstream service receives request with headers
2. Validates `X-Source` header
3. Sets Spring Security context from headers
4. Applies method-level security rules

## ⚙️ Configuration Highlights

### Gateway Security
- Stateless JWT authentication
- Role-based route protection
- Header propagation to downstream services
- Open endpoints configuration

### Downstream Security
- Header trust model
- Method-level access control
- Flexible authorization rules
- Defense in depth architecture

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Spring Boot 3.+
- Spring Cloud Gateway
- JWT tokens

### Dependencies
```xml
<!-- Gateway -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Downstream -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>