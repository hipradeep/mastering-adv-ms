# Low Level Design (LLD) - Terminator Project

## 1. Database Design (Schema)

The system is built on a Service-per-DB pattern using PostgreSQL. Below are the schema definitions for the core tables.

### 1.1 Orchestrator Service
**Table:** `hstt_saga_transactions` (Schema: `saga`)
Tracks the lifecycle of distributed transactions.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `hststr_transaction_id` | VARCHAR | **PK** | Unique UUID for the Saga. |
| `hststr_status` | VARCHAR | Enum | `STARTED`, `SUCCESS`, `FAILURE`. |
| `hststr_request_payload` | TEXT | | JSON dump of the original `IssueRequestDto`. |
| `hststr_failure_reason` | VARCHAR | | Error message if failed. |
| `gnum_hospital_code` | INTEGER | | Context identifier (e.g., 998). |
| `hstdt_completion_date` | TIMESTAMP | | Time when saga ended. |
| `gdt_entry_date` | TIMESTAMP | | Audit: Creation time. |

### 1.2 Inventory Service
**Table:** `hstt_drug_currstock_dtl` (Schema: `dwh`)
Holds the real-time stock position for items in stores.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `hstnum_store_id` | INTEGER | **PK** | Store Identifier. |
| `hstnum_itembrand_id` | INTEGER | **PK** | Drug/Item Identifier. |
| `hststr_batch_no` | VARCHAR | **PK** | Specific Batch Number. |
| `hstnum_mfg_id` | INTEGER | **PK** | Manufacturer ID. |
| `hstnum_programme_id` | INTEGER | **PK** | Programme/Scheme ID. |
| `gnum_hospital_code` | INTEGER | **PK** | Hospital Code. |
| `hstnum_stock_status_code` | INTEGER | **PK** | Status (e.g., 10=Active). |
| `hstnum_inhand_qty` | DOUBLE | | **Critical**: Current usable quantity. |
| `hstnum_reserved_qty` | DOUBLE | | Quantity locked by ongoing Sagas. |
| `hstdt_expiry_date` | DATE | | Batch expiry date. |
| `hstnum_rate` | DOUBLE | | Purchase rate per unit. |

**Table:** `hstt_stock_transaction_dtl`
Audit trail for every stock movement.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `hstnum_trans_id` | BIGINT | **PK**, Auto | Audit ID. |
| `hststr_saga_trans_id` | VARCHAR | | Link to Orchestrator Saga. |
| `hststr_trans_type` | VARCHAR | | `RESERVE` or `RELEASE`. |
| `hstnum_transfer_qty` | DOUBLE | | Quantity changed. |
| `hstnum_inhand_qty_before`| DOUBLE | | Snapshot before change. |
| `hstnum_inhand_qty_after` | DOUBLE | | Snapshot after change. |

### 1.3 Issue Service
**Table:** `hstt_patemp_issue_dtl`
Header table for a patient issue transaction.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `hstnum_issue_no` | INTEGER | **PK** | Unique Issue Number. |
| `hstnum_store_id` | INTEGER | **PK** | Issuing Store. |
| `gnum_hospital_code` | INTEGER | **PK** | Hospital Code. |
| `hrgnum_puk` | VARCHAR | | Patient Unique Key (CR No). |
| `hstdt_issue_date` | DATE | | Date of issue. |
| `hstnum_net_cost` | DOUBLE | | Total cost of issue. |

**Table:** `hstt_patemp_issue_item_dtl`
Line items for the issue (one row per drug/batch).

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `hstnum_issue_no` | INTEGER | **PK**, FK | Link to Header. |
| `hstnum_itembrand_id` | INTEGER | **PK** | Drug ID. |
| `hststr_batch_sl_no` | VARCHAR | **PK** | Batch Number. |
| `hstnum_issue_qty` | DOUBLE | | Quantity issued. |
| `hstnum_rate` | DOUBLE | | Rate applied. |

---

## 2. API Specifications

### 2.1 Orchestrator Service
**Endpoint:** `POST /api/orchestrator/issue`
Initiates the Issue Drug Saga.

*   **Request Body** (`IssueRequestDto`):
    ```json
    {
      "gnumHospitalCode": 998,
      "hstnumStoreId": 101,
      "hrgnumPuk": "1000001",
      "issueItems": [
        {
          "hstnumItemBrandId": 5001,
          "hststrBatchNo": "B001",
          "hstnumIssueQty": 10
        }
      ]
    }
    ```
*   **Response**:
    ```json
    {
      "status": "success",
      "message": "Issue Process Initiated. Transaction ID: 550e8400-e29b...",
      "data": "550e8400-e29b..."
    }
    ```

**Endpoint:** `GET /api/orchestrator/status/{transactionId}`
Polls the status of the background Saga.

*   **Response**:
    ```json
    {
      "status": "success",
      "message": "Transaction Status Fetched",
      "data": "SUCCESS" 
    }
    ```
    *Possible Data Values*: `STARTED`, `SUCCESS`, `FAILURE`.

### 2.2 Inventory Service
**Endpoint:** `GET /api/inventory/stock`
Fetches available stock for the UI.

*   **Parameters**: `hospitalCode` (default 998), `storeId`, `itemBrandId`.
*   **Response**: List of `StockResponseDto` containing Batch No, InHand Qty, Expiry Date.

**Endpoint:** `POST /api/inventory/stock/update`
Direct stock update (Internal/Admin use).

*   **Parameters**: `storeId`, `itemBrandId`, `batchNo`, `quantity`.

### 2.3 Issue Service
**Endpoint:** `POST /api/issue/create`
Creates the permanent issue record. Use primarily by Kafka Consumer, but exposed for testing.

*   **Request Body**: `IssueRequestDto` (Same as Orchestrator).
*   **Response**: `IssueResponseDto` with generated `Issue No`.

---

## 3. Implementation Logic Details

### 3.1 Atomic Inventory Updates
To prevent Race Conditions (Overselling) without using performance-heavy database locks (Pessimistic Locking), the system uses **Atomic Native SQL Queries** in `DrugCurrstockDtlRepository`.

**Logic:**
```sql
UPDATE dwh.hstt_drug_currstock_dtl 
SET hstnum_inhand_qty = hstnum_inhand_qty - :deductQty 
WHERE hstnum_store_id = :storeId 
  AND hstnum_itembrand_id = :itemBrandId 
  AND hststr_batch_no = :batchNo 
  AND hstnum_inhand_qty >= :deductQty
```

*   **Explanation**: The condition `hstnum_inhand_qty >= :deductQty` ensures that if two users try to deduct the last 10 units simultaneously, the database will only allow the first one to succeed (Row Count = 1). The second one will fail (Row Count = 0) because the condition is no longer met.

### 3.2 Saga Compensation
If the **Issue Service** fails to save the record (e.g., DB Constraint Violation):
1.  Issue Service publishes `IssueCreatedEvent` with `success=false`.
2.  Orchestrator receives failure event.
3.  Orchestrator sends `ConfirmStockCommand` with `commit=false`.
4.  Inventory Service receives command and **Rolls Back** the stock deduction:
    ```sql
    UPDATE dwh.hstt_drug_currstock_dtl 
    SET hstnum_inhand_qty = hstnum_inhand_qty + :qty 
    ...
    ```
5.  Saga Status updated to `FAILURE`.
