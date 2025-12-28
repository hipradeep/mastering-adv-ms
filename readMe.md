# Mastering Advanced Microservices

A multi-module Maven project demonstrating microservices architecture using Spring Boot.

## Modules
- `common-lib` - Shared utilities and DTOs
- `gateway-service` - API Gateway
- `config-server` - Configuration server
- `discovery-server` (eureka-server) - Service Registry
- `auth-server` - Authentication Service
- `user-service` - User management service (Integrated with PostgreSQL)
- `order-service` - Order management
- `inventory-service` - Inventory management
- `notification-service` - Notification service

## Persistence
The **User Service** now uses **PostgreSQL** for data persistence.

### Configuration
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA (Hibernate)
- **Connection**: Configured in `application.yml` (defaults to `jdbc:postgresql://localhost:5432/users_datebase`).

### Key Components
- **User Entity**: Annotated with `@Entity` and `@Table(name = "users")`.
- **Repository**: `UserRepository` extends `JpaRepository<User, Long>` for standard CRUD and paging operations.
- **Service Layer**: Handles business logic and transaction management.

## Caching Layer
The **User Service** utilizes **Redis** to cache frequently accessed data.

### Configuration
- **Provider**: Redis (Jedis)
- **Enabled**: Via `@EnableCaching` in `UserServiceApplication`.
- **Connection**: `localhost:6379` (Configurable in `application.yml`).

### Caching Strategy
- **Read-Through**:
    - Method: `getUserById(Long id)`
    - Annotation: `@Cacheable(value = "users", key = "#id")`
    - Behavior: Checks Redis first. If missing, executes SQL, returns result, and stores it in Redis.
- **Write-Through**:
    - Method: `updateUser(Long id, User user)`
    - Annotation: `@CachePut(value = "users", key = "#id")`
    - Behavior: Updates the DB and immediately refreshes the cache entry with the new data.
- **Eviction**:
    - Method: `deleteUser(Long id)`
    - Annotation: `@CacheEvict(value = "users", key = "#id")`
    - Behavior: Removes the user from Redis when deleted from their database.

### Custom Configuration (`CacheConfig.java`)
- **Default TTL**: 30 Minutes.
- **Specific Caches**:
  - `users`: **2 Minutes**.
  - `likes`: **1 Hour**.
- **Serialization**: JSON (GenericJackson2JsonRedisSerializer) for readable values.
- **Null Values**: Disabled.

---

## Build & Run

All services can be run using Maven.

### Command Pattern
To build and run a service (skipping tests for speed):
```bash
cd [service-name] && mvn clean install -DskipTests && mvn spring-boot:run
```

### Services
- **Auth Server**: `auth-server`
- **Config Server**: `config-server`
- **Eureka Server**: `eureka-server`
- **Gateway Service**: `gateway-service`
- **Inventory Service**: `inventory-service`
- **Notification Service**: `notification-service`
- **Order Service**: `order-service`
- **User Service**: `user-service`
