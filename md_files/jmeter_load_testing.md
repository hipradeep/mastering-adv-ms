# JMeter Load Testing Guide for Terminator

This document outlines the configuration, test plan, and execution steps for load testing the Terminator application (Issue Drug to Patient flow).

## 1. Environment & Configuration

### JMeter Installation
- **Location**: `D:\Projects Workspaces\terminator\jmeter`
- **Binary**: `D:\Projects Workspaces\terminator\jmeter\bin\jmeter.bat`

### Test Script
- **Location**: `D:\Projects Workspaces\terminator\tests\terminator_load_test.jmx`

### Configuration Variables
The test plan uses **User Defined Variables** for easy configuration. These can be modified in the JMX file or overridden via command line properties.

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `HOST` | `10.226.30.45` | Application Server IP |
| `PORT_ISSUE` | `8081` | Port for Issue To Patient Service |
| `PORT_INVENTORY` | `8082` | Port for Inventory Service |
| `PORT_ORCHESTRATOR`| `8083` | Port for Orchestrator Service |
| `STORE_ID` | `10101` | Test Store ID |
| `ITEM_ID` | `1010101` | Test Item ID |
| `BATCH_NO` | `BATCH001` | Batch No available in stock |
| `PUK` | `123456789` | Patient Unique Key |

## 2. Test Plan Overview

The test simulates a user navigating the Issue Page and performing an issuance.

### Thread Group
- **Users (Threads)**: 1 (Default) - *Increase this to simulate load*
- **Ramp-Up Period**: 1s
- **Loop Count**: 1

### HTTP Requests
1.  **Get Latest Issues** (`GET /api/issue/latest`)
    -   Fetches the list of previously issued drugs for the store.
2.  **Get Stock** (`GET /api/inventory/stock`)
    -   Checks availability of the item in the store.
3.  **Initiate Issue** (`POST /api/orchestrator/issue`)
    -   Submits the drug issuance request to the orchestrator.
    -   **Payload**: Uses variables `${STORE_ID}`, `${PUK}`, `${ITEM_ID}`, `${BATCH_NO}`.
    -   **Extractors**: Captures new Transaction ID from response (JSON Extractor).

## 3. How to Run

### Option A: Command Line (CLI) - Recommended for Load Testing
Run the test in non-GUI mode for better performance results.

**Run Command:**
```powershell
& "D:\Projects Workspaces\terminator\jmeter\bin\jmeter.bat" -n -t "D:\Projects Workspaces\terminator\tests\terminator_load_test.jmx" -l "D:\Projects Workspaces\terminator\tests\results.jtl"
```

**Generate HTML Report:**
After the test finishes, generate a dashboard from the results:
```powershell
& "D:\Projects Workspaces\terminator\jmeter\bin\jmeter.bat" -g "D:\Projects Workspaces\terminator\tests\results.jtl" -o "D:\Projects Workspaces\terminator\tests\report"
```

### Option B: JMeter GUI - Recommended for Debugging
1.  Open `D:\Projects Workspaces\terminator\jmeter\bin\jmeter.bat`.
2.  File > Open > `D:\Projects Workspaces\terminator\tests\terminator_load_test.jmx`.
3.  Click the Green "Start" button.
4.  View "View Results Tree" listener for details.

## 4. Viewing Results
-   **CLI Results**: Check `D:\Projects Workspaces\terminator\tests\results.jtl` (CSV format).
-   **HTML Report**: Open `D:\Projects Workspaces\terminator\tests\report\index.html` in your browser.
