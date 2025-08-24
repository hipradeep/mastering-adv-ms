# Mastering Advanced Microservices

A multi-module Maven project demonstrating microservices architecture.

## Modules
- `common-lib` - Shared utilities and DTOs
- `gateway-service` - API Gateway
- `config-server` - Configuration server
- `user-service` - User management service

# Build && Run 
## clean, compile, install and test
### auth-server
```bash
cd auth-server && mvn clean install && mvn spring-boot:run
```

### config-server
```bash
cd config-server && mvn clean install && mvn spring-boot:run
```

### eureka-server
```bash
cd eureka-server && mvn clean install && mvn spring-boot:run
```

### gateway-service
```bash
cd gateway-service && mvn clean install && mvn spring-boot:run
```

### inventory-service
```bash
cd inventory-service && mvn clean install && mvn spring-boot:run
```

### notification-service
```bash
cd notification-server && mvn clean install && mvn spring-boot:run
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
### auth-server
```bash
cd auth-server && mvn clean install -DskipTests  && mvn spring-boot:run
```

### config-server
```bash
cd config-server && mvn clean install -DskipTests  && mvn spring-boot:run
```

### eureka-server
```bash
cd eureka-server && mvn clean install -DskipTests  && mvn spring-boot:run
```

### gateway-service
```bash
cd gateway-service && mvn clean install -DskipTests  && mvn spring-boot:run
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
cd user-seribice && mvn clean install -DskipTests  && mvn spring-boot:run
```
