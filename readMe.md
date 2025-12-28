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

# Distributed Tracing (User Service)
Implemented using **Micrometer Tracing** (Brave) and **Zipkin** for Spring Boot 3.x.
*Note: `spring-cloud-starter-sleuth` is removed in Spring Boot 3.x and replaced by Micrometer Tracing.*

### Dependencies
- `spring-boot-starter-actuator`
- `micrometer-tracing-bridge-brave`
- `zipkin-reporter-brave`

### Configuration
`application.yml`:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### Verification
1. **Run Zipkin**: `docker run -d -p 9411:9411 openzipkin/zipkin`
2. **Access UI**: [http://localhost:9411](http://localhost:9411)
3. **Simulate Trace**: `curl http://localhost:8082/api/users/trace`
4. **Result**: Check logs for `[user-service,traceId,spanId]` and view trace in Zipkin UI.

### Comparison: Spring Cloud Sleuth (Boot 2.x) vs. Micrometer Tracing (Boot 3.x)

| Feature | Spring Boot 2.x (Sleuth) | Spring Boot 3.x (Micrometer Tracing) |
| :--- | :--- | :--- |
| **Library** | `spring-cloud-starter-sleuth` | **Removed**. Replaced by Micrometer Tracing. |
| **Tracing Core** | Brave (shaded/internal) | `micrometer-tracing-bridge-brave` or `...-otel` |
| **Reporting** | `spring-cloud-sleuth-zipkin` | `zipkin-reporter-brave` |
| **Actuator** | Integrated | Requires `spring-boot-starter-actuator` explicitly |
| **Status** | Maintenance / End of Life | **Current Standard** |
