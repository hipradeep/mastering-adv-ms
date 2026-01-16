# State of the Project (Final Health Check)

## ✅ Resolved Critical Issues

1.  **Zombie Transaction Risk (The Double Write Problem)**
    -   **Context:** `OrchestratorService` saved to DB but ignored Kafka failures, leading to potential data inconsistency.
    -   **Status:** **FIXED.** Added `@Transactional` and made Kafka sending synchronous. If Kafka fails, the DB record is now rolled back.
2.  **Frontend Memory Leak**
    -   **Context:** `IssuePage.js` had an unmanaged `setInterval` that continued running after navigation.
    -   **Status:** **FIXED.** Refactored to use `useEffect` which automatically cleans up the polling interval on component unmount.
3.  **Fire-and-Forget Kafka Errors**
    -   **Context:** `KafkaProducerService` ignored send errors.
    -   **Status:** **FIXED.** Now waits for broker acknowledgement to ensure data safety.
4.  **Logging Conflicts**
    -   **Status:** **FIXED.** `inventory-service` and `issue-to-patient-service` now write to separate log files.
5.  **Schema Management**
    -   **Status:** **FIXED.** `orchestrator-service` aligned to `ddl-auto: none`.

---

## ⚠️ Remaining Technical Debt (Action Recommended)

### 1. Hardcoded Security Credentials
-   **Location:** `application.yml` (e.g., lines 9-10 in Orchestrator).
-   **Issue:** Username `dwh` and password `dwh` are committed to code.
-   **Recommendation:** Move to Environment Variables (`${DB_USER}`, `${DB_PASS}`).

### 2. Hardcoded Infrastructure IPs
-   **Location:** `application.yml` (`10.10.10.12`).
-   **Issue:** Application breaks if DB moves to a new server (e.g., changes to `10.10.10.13`).
-   **Recommendation:** Use DNS names or Environment variables.

### 3. Magic Numbers in Code
-   **Location:** `OrchestratorController.java` (Line 27).
-   **Issue:** `requestDto.setGnumHospitalCode(998);`
-   **Recommendation:** Define `998` as a named constant (e.g., `DEFAULT_HOSPITAL_CODE`).

### 4. Legacy Frontend Tooling
-   **Location:** `package.json`.
-   **Issue:** Using `react-scripts`.
-   **Recommendation:** Upgrade to **Vite** for faster build times in the future.

---

## Conclusion
The **critical reliability and stability bugs** have been squashed. The remaining issues are primarily **configuration/security best practices** which can be addressed in a future refactoring sprint.
