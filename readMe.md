# Mastering Advanced Microservices

A monorepo containing multiple independent Spring Boot microservices demonstrating microservices architecture. Each service is standalone and managed in a single repository.

## Modules / Services

- `auth-server` – Handles authentication, JWT token generation, and validation.
- `eureka-server` – Service registry for service discovery.
- `api-gateway` – API Gateway to route requests to microservices.
- `inventory-service` – Manages product inventory and stock levels.
- `notification-service` – Sends notifications via email/SMS or other channels.
- `order-service` – Handles order creation, tracking, and management.
- `user-service` – Manages user data, authentication, and profiles.

---
## Branch & Feature Addition Guidelines

**Branch Name**: `feature/gateway`
- Implementation **Configure Spring Cloud Gateway**.

---
# Build && Run 
## clean, compile, install and test
### auth-server
```bash
cd auth-server && mvn clean install && mvn spring-boot:run
```


### eureka-server
```bash
cd eureka-server && mvn clean install && mvn spring-boot:run
```

### api-gateway
```bash
cd api-gateway && mvn clean install && mvn spring-boot:run
```

### inventory-service
```bash
cd inventory-service && mvn clean install && mvn spring-boot:run
```

### notification-service
```bash
cd notification-service && mvn clean install && mvn spring-boot:run
```

### order-service
```bash
cd order-service && mvn clean install && mvn spring-boot:run
```

### user-service
```bash
cd user-seribice && mvn clean install && mvn spring-boot:run
```

# Build && Run
## clean, compile, install and without test

### eureka-server
```bash
cd eureka-server && mvn clean install -DskipTests  && mvn spring-boot:run
```

### api-gateway
```bash
cd api-gateway && mvn clean install -DskipTests  && mvn spring-boot:run
```

### auth-server
```bash
cd auth-server && mvn clean install -DskipTests  && mvn spring-boot:run
```

### inventory-service
```bash
cd inventory-service && mvn clean install -DskipTests  && mvn spring-boot:run
```

### notification-service
```bash
cd notification-server && mvn clean install -DskipTests  && mvn spring-boot:run
```

### order-service
```bash
cd order-service && mvn clean install -DskipTests  && mvn spring-boot:run
```

### user-service
```bash
cd user-service && mvn clean install -DskipTests  && mvn spring-boot:run
```
