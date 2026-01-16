# Project Health & Issues Report

Based on a scan of the `terminator` workspace, the following potential issues have been identified. They are categorized by severity.

## 🚨 High Severity (Action Required)

### 1. Hardcoded Security Credentials
**Found in:** `orchestrator-service`, `inventory-service`, `issue-to-patient-service` (`application.yml`)
- All services have the database username and password hardcoded as `dwh` / `dwh`.
- **Risk:** Security vulnerability. If this code is committed to a public repo, credentials are exposed.
- **Recommendation:** Use environment variables (e.g., `${DB_USERNAME}`, `${DB_PASSWORD}`) or a secrets manager.

### 2. Log File Conflict
**Found in:** `inventory-service`, `issue-to-patient-service`
- Both services are configured to write to the exact same log file: `logs/hp-rc-service.log`.
- **Risk:** If run on the same machine, logs will be interleaved or overwritten, making debugging impossible.
- **Recommendation:** Change `logging.file.name` to unique paths (e.g., `logs/inventory-service.log`).

### 3. Hardcoded Infrastructure IPs
**Found in:** All services
- Database URL is hardcoded to `10.10.10.12`.
- Kafka bootstrap server in Orchestrator is `localhost:9092`.
- **Risk:** Brittle configuration. Moving to a different environment (or if DHCP changes the IP) requires code recompilation.
- **Recommendation:** Externalize hostnames/IPs to environment variables or use Service Discovery.

## ⚠️ Medium Severity (Improvements)

### 4. Spring Boot Version Mismatch
**Found in:** `pom.xml` files
- `orchestrator-service`: **v3.4.1**
- `inventory-service` & `issue-to-patient-service`: **v3.2.0**
- **Risk:** Inconsistent behavior or compatibility issues between services. Maintenance overhead.
- **Recommendation:** Align all services to the same Spring Boot version (preferably the latest, 3.4.1).

### 5. Legacy Frontend Tooling
**Found in:** `frontend/package.json`
- The project uses `react-scripts` (Create React App).
- **Risk:** CRA is largely considered deprecated/legacy (slow builds, outdated dependencies).
- **Recommendation:** Consider migrating to **Vite** for faster builds and better modern support.

## ℹ️ Low Severity / Notes

- **Orchestrator Kafka Config:** `orchestrator-service` sets `application.name` but others do not.
- **Schema Management:** `orchestrator` is set to `ddl-auto: update`, while others are `none`. Ensure this is intentional (Orchestrator owns the schema).
- **Missing Dependency Management:** `orchestrator-service` defines a `spring-cloud.version` property but does not seem to include the `dependencyManagement` block to actually use it (based on the view).

---

## Proposed Next Steps

If you would like, I can help fix these issues. Please select from the following:

1.  **Refactor Configuration:** Externalize secrets and IPs to environment variables.
2.  **Fix Logging:** Update log file paths to be unique.
3.  **Align Versions:** Upgrade all services to Spring Boot 3.4.1.
