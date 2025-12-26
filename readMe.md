# Mastering Advanced Microservices

A multi-module Maven project demonstrating microservices architecture using Spring Boot.

## Modules
- `common-lib` - Shared utilities and DTOs
- `gateway-service` - API Gateway
- `config-server` - Configuration server
- `discovery-server` (eureka-server) - Service Registry
- `auth-server` - Authentication Service
- `user-service` - User management service (Integrated with Redis)
- `order-service` - Order management
- `inventory-service` - Inventory management
- `notification-service` - Notification service

## Redis Implementation in User Service

We support two approaches for integrating Redis in Spring Boot. Below is an explanation of both.

### 1. Using RedisTemplate (Manual DAO Approach)
This approach gives detailed control over Redis commands. You manually serialize/deserialize objects and use `RedisTemplate` methods (like `opsForValue`, `opsForHash`) to interact with Redis.

**Key Components:**
- **RedisTemplate**: The central class for Redis interaction.
- **DAO (Data Access Object)**: A class where you write manual CRUD logic (e.g., `redisTemplate.opsForHash().put(...)`).
- **Entity**: Must implement `Serializable`.

**Pros:**
- Complete control over data structure and storage format.
- Can optimize specific Redis commands.

**Cons:**
- More boilerplate code.
- Requires manual mapping and error handling.

### 2. Using Spring Data Redis Repositories (Current Implementation)
This approach abstracts Redis operations behind the familiar Spring Data `Repository` interface, similar to JPA/Hibernate.

**Key Components:**
- **@RedisHash**: Annotates the entity class (e.g., `@RedisHash("User")`). This tells Spring to create a Hash in Redis with the given prefix.
- **@Id**: Annotates the identifier field.
- **Repository Interface**: An interface extending `CrudRepository` or `PagingAndSortingRepository`. Spring automatically implements methods like `save()`, `findById()`, `findAll()`, etc.

**Pros:**
- Very little boilerplate (just an interface).
- Familiar API for developers used to Spring Data JPA.
- Automatic handling of object mapping and key management.

**Cons:**
- Less control over the underlying Redis commands and data structure.
- Might be harder to debug complex queries or performance issues.

**Differences Summary**

| Feature | RedisTemplate | Redis Repositories |
| :--- | :--- | :--- |
| **Abstraction Level** | Low (Closer to Redis commands) | High (Spring Data abstraction) |
| **Boilerplate** | High (Manual DAO) | Low (Interface only) |
| **Ease of Use** | Moderate | High |
| **Entity Config** | `Serializable` | `@RedisHash`, `@Id` |
| **Storage Model** | **Single Giant Hash** (key=`USER`, field=id) | **Separate Keys** (key=`User:id`) |
| **TTL Support** | Difficult (Expires whole hash) | Built-in (Per-entity TTL) |
| **Secondary Indexes** | Manual maintenance required | Automatic via `@Indexed` |

### 3. Implemented Features
We have enhanced the **Redis Repository** implementation with the following:

- **Secondary Indexes**:
  - Enabled via `@Indexed` annotation on `username` and `email` fields.
  - Allows lookup by these fields using standard repository methods: `findByUsername(String username)` and `findByEmail(String email)`.

- **Time-To-Live (TTL)**:
  - Configured via `@RedisHash(value = "User", timeToLive = 600)`.
  - User records automatically expire and are removed from Redis after **10 minutes** (600 seconds).
---
 


