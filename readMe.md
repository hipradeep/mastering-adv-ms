# Mastering Advanced Microservices

A multi-module Maven project demonstrating microservices architecture.

## Modules
- `common-lib` - Shared utilities and DTOs
- `gateway-service` - API Gateway
- `config-server` - Configuration server
- `user-service` - User management service

## Build
```bash
mvn clean install
```

# Run
### Build all modules
```bash
mvn clean package
```
### Run specific service
```bash
cd user-service && mvn spring-boot:run
```



## 7. 🚀 Build and Run Commands

### **Build Entire Project:**
```bash
# From root directory
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Build specific module
mvn -pl user-service clean install


# Run gateway service
cd gateway-service
mvn spring-boot:run

# Run config server  
cd config-server
mvn spring-boot:run

# Run user service
cd user-service
mvn spring-boot:run


# Build all Docker images
docker-compose build

# Build specific service
docker-compose build user-service