# Mastering Advanced Microservices

A multi-module Maven project demonstrating microservices architecture.

## Modules
- `auth-server` - Authentication server
- `common-lib` - Shared utilities and DTOs
- `config-server` - Configuration server
- `eureka-server` - Service discovery server
- `gateway-service` - API Gateway
- `inventory-service` - Inventory management
- `notification-service` - Notification service
- `order-service` - Order management
- `user-service` - User management


## ELK Stack Installation Guide (Windows)

Follow these steps to download, install, and run the ELK stack components.

### 1. Elasticsearch
1.  **Download**: [Elasticsearch Download](https://www.elastic.co/downloads/elasticsearch) (Windows).
2.  **Install**: Unzip to `C:\elk\elasticsearch`.
3.  **Run**:
    ```cmd
    cd C:\elk\elasticsearch\bin
    elasticsearch.bat
    ```
4.  **Verify**: http://localhost:9200

### 2. Kibana
1.  **Download**: [Kibana Download](https://www.elastic.co/downloads/kibana) (Windows).
2.  **Install**: Unzip to `C:\elk\kibana`.
3.  **Config**: Edit(Uncomment) `config\kibana.yml`, set `elasticsearch.hosts: ["http://localhost:9200"]`.
4.  **Run**:
    ```cmd
    cd C:\elk\kibana\bin
    kibana.bat
    ```
5.  **Verify**: http://localhost:5601

### 3. Logstash
1.  **Download**: [Logstash Download](https://www.elastic.co/downloads/logstash) (ZIP).
2.  **Install**: Unzip to `C:\elk\logstash`.
3.  **Run**: See the **Integration** section below.

---

## ELK Stack Integration (Manual Setup)

This project is configured to send logs to the ELK stack (Elasticsearch, Logstash, Kibana) using `LogstashTcpSocketAppender`.

### 1. Prerequisites
Ensure you have the following installed manually on your machine:
- **Elasticsearch**: Running on `localhost:9200`
- **Logstash**: With the configured pipeline
- **Kibana**: Running on `localhost:5601`

### 2. Configure & Run Logstash
A `logstash.conf` file is provided in the project root. This configuration listens on specific ports and also reads from log files.

**Run Logstash:**
Navigate to your Logstash `bin` directory and run:
```bash
logstash -f "c:\Users\hipradeep\Documents\mastering-adv-ms\logstash.conf"
```
*Note: Ensure the path in the command matches the actual location of `logstash.conf`.*

The configuration includes:
- **TCP Input**: Port `5000` (JSON codec) - used by Spring Boot apps directly.
- **File Input**: Reads `*.log` files from `c:/Users/hipradeep/Documents/mastering-adv-ms/logs/`.
- **Output**: Sends logs to Elasticsearch index `spring-boot-logs-YYYY.MM.dd`.

### 3. Verification
1.  Start Logstash (wait for "Successfully started Logstash API endpoint").
2.  Start your Spring Boot services.
3.  Open Kibana (http://localhost:5601).
4.  Go to **Stack Management > Index Patterns**.
5.  Create a new index pattern for `spring-boot-logs-*`.
6.  Go to **Discover** to view your centralized logs.
