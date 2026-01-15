# AWS RDS (Relational Database Service) Deep Dive

## 1. Supported Engines
*   **Amazon Aurora**: MySQL and PostgreSQL compatible. Built for the cloud (3x faster than Standard PostgreSQL, 5x faster than Standard MySQL).
*   **Open Source**: PostgreSQL, MySQL, MariaDB.
*   **Commercial**: Oracle, Microsoft SQL Server.

## 2. Architecture & High Availability
### Multi-AZ Deployment (High Availability)
*   **Synchronous Replication**: Writes are replicated to a standby instance in a different Availability Zone (AZ).
*   **Automatic Failover**: If the primary fails, AWS updates the DNS record to point to the standby.
*   **Use Case**: Production workloads requiring high availability. NOT for performance scaling.

### Read Replicas (Scalability)
*   **Asynchronous Replication**: Data is copied to Read Replicas.
*   **Read Scaling**: Offload read traffic (reporting, analytics) from the master DB.
*   **Use Case**: Read-heavy workloads. Slower writes (due to replication lag).

## 3. Security
*   **Encryption**:
    *   **At Rest**: Uses AWS KMS (AES-256). Must be defined at creation time.
    *   **In Transit**: SSL/TLS.
*   **Security Groups**: Controls network access to the DB. Ensure only the EC2 Security Group (not 0.0.0.0/0) is allowed on port 3306/5432.
*   **IAM Database Authentication**: Use IAM Roles instead of a password.

## 4. Features
*   **Automated Backups**: Daily full snapshot + transaction logs (5-minute Point-in-Time Recovery).
*   **Maintenance Windows**: Define when OS/DB patching occurs.
*   **RDS Proxy**: Pools connections to the DB. critical for Lambda functions (prevents opening too many connections).

## 5. Spring Boot & Microservices Integration
### Datasource Configuration
In `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://my-rds-endpoint.us-east-1.rds.amazonaws.com:3306/mydb
spring.datasource.username=admin
# Use Environment Variables or Secrets Manager!
spring.datasource.password=${DB_PASSWORD}
```

### IAM Database Authentication
Instead of hardcoding a password, use an authentication token.
1.  **Enable IAM Auth** on the RDS instance.
2.  **Attach Policy** to the EC2 IAM Role allowing `rds-db:connect`.
3.  **Spring Boot Code**:
    *   Use the AWS SDK `RdsIamTokenGenerator` to generate a token.
    *   Pass this token as the password to the JDBC driver.

### Connection Pooling (HikariCP)
Spring Boot uses HikariCP by default. For RDS Multi-AZ, ensure proper timeout settings so the pool recovers quickly after a failover:
```properties
# Maximum time to wait for a connection
spring.datasource.hikari.connection-timeout=20000
# Max time a connection can sit idle
spring.datasource.hikari.idle-timeout=300000
# Max lifetime of a connection (important for DNS updates during failover)
spring.datasource.hikari.max-lifetime=900000
```
