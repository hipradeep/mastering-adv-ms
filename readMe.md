# Mastering Advanced Microservices

A multi-module Maven project demonstrating microservices architecture.

## Modules
- `common-lib` - Shared utilities and DTOs
- `gateway-service` - API Gateway
- `config-server` - Configuration server
- `user-service` - User management service

# API Documentation

## User Service (Port: 8082)
Base URL: `http://localhost:8082`

### Create User
```cmd
curl -X POST http://localhost:8082/api/user -H "Content-Type: application/json" -d "{\"username\": \"john_doe\", \"email\": \"john@example.com\", \"password\": \"securepassword123\"}"
```

### Get All Users
```cmd
curl -X GET http://localhost:8082/api/user
```

### Get User by ID
```cmd
curl -X GET http://localhost:8082/api/user/1
```

### Update User
```cmd
curl -X PUT http://localhost:8082/api/user/1 -H "Content-Type: application/json" -d "{\"username\": \"john_doe_updated\", \"email\": \"john_new@example.com\", \"password\": \"newpassword456\"}"
```

### Delete User
```cmd
curl -X DELETE http://localhost:8082/api/user/1
```



# Logging Implementation

We have implemented a robust logging strategy using `logback-spring.xml` and SLF4J.

## Why this approach is better?

1.  **Centralized Configuration (`logback-spring.xml`)**:
    -   Instead of relying on default Spring Boot logging, we use a custom XML configuration.
    -   **Rolling File Appender**: Logs are written to the `logs/` directory. Files are rolled over daily or when they reach 10MB. This prevents a single massive log file from consuming all disk space and makes archiving easier.
    -   **Console Output**: Logs are also printed to the console for real-time debugging during development.

2.  **Explicit Logger Definition (UserService)**:
    -   In `UserService`, we used:
        ```java
        private static final Logger log = LoggerFactory.getLogger(UserService.class);
        ```
    -   **Benefit**: While Lombok's `@Slf4j` is convenient, explicit declaration removes dependency on annotation processing magic. It ensures that the logger is always associated with the correct class context and is standard Java practice, making the code more readable and portable even without Lombok.

3.  **Structured Logging**:
    -   The pattern `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n` ensures every log entry has a precise timestamp, thread name, log level, and source, which is critical for debugging distributed microservices.

4.  **ELK Stack Compatibility**:
    -   **File-Based Ingestion**: By writing logs to the `logs/` directory, we prepare the system for easy integration with the ELK (Elasticsearch, Logstash, Kibana) stack.
    -   **Logstash/Filebeat**: Tools like **Filebeat** or **Logstash** can be configured to "tail" these log files in real-time.
    -   **Decoupling**: This approach decouples the application from the logging infrastructure. The microservices don't need to know about Elasticsearch; they simply write to disk. This improves performance and reliability (if Elasticsearch is down, the app keeps running and logging locally).
    -   **Parsing**: The structured log pattern defined in `logback-spring.xml` can be easily parsed by Logstash `grok` filters to index fields like timestamp, severity, and service name into Elasticsearch for powerful visualization in Kibana.
