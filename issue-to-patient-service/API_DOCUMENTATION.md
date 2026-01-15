# Issue to Patient Service API Documentation

This service manages the issuing of items/drugs to patients.

**Base URL**: `/api/issue`

## 1. Create Issue
Creates a new issue record for a patient.

- **URL**: `/create`
- **Method**: `POST`
- **Content-Type**: `application/json`

### Request Body (`IssueRequestDto`)
```json
{
  "hstnumStoreId": 10101,
  "gnumHospitalCode": 101,
  "hrgnumPuk": "101010101",
  "hststrRecieveBy": "Patient Relative",
  "gstrRemarks": "Urgent Issue",
  "sstnumReqtypeId": 1,
  "sstnumItemCatNo": 10,
  "gnumDeptCode": 105,
  "gnumSeatid": 1001,
  "hststrPatientName": "John Doe",
  "hstdtAge": "1990-01-01", 
  "gnumGenderCode": 1,
  "items": [
    {
      "hstnumItembrandId": 5001,
      "hstnumItemId": 501,
      "hststrBatchSlNo": "BATCH001",
      "hstnumIssueQty": 10,
      "hstnumIssueqtyUnitid": 6301,
      "hstnumRate": 5.50,
      "hstnumRateUnitid": 6301,
      "hstnumMfgId": 2001,
      "hstdtExpiryDate": "2027-12-31",
      "hstnumStockStatusCode": 1,
      "hstnumGroupId": 10,
      "hstnumSubgroupId": 101,
      "hststrItemSlNo": "0",
      "hstnumConsumableFlag": 1
    }
  ]
}
```
*Note: `hstdtAge` is mapped as a Date, ensure correct format if passed as string or handle as timestamp.*

### Success Response (`ApiResponse<String>`)
**Status**: `201 Created`
```json
{
  "success": true,
  "message": "Issue created successfully",
  "data": "Issue Generated Successfully. Issue No: 1005"
}
```

## 2. Get Issue Details
Retrieves details of a specific issue and its items.

- **URL**: `/details`
- **Method**: `GET`

### Query Parameters
| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `hospitalCode` | Integer | Yes | The hospital code (e.g., 101) |
| `storeId` | Integer | Yes | The store ID (e.g., 10101) |
| `issueNo` | Integer | Yes | The issue number to fetch |

### Success Response (`ApiResponse<IssueResponseDto>`)
**Status**: `200 OK`
```json
{
  "success": true,
  "message": "Issue details fetched successfully",
  "data": {
    "hstnumStoreId": 10101,
    "hstnumIssueNo": 1005,
    "gnumHospitalCode": 101,
    "hrgnumPuk": "101010101",
    "hststrPatientName": "John Doe",
    "items": [
      {
        "hstnumItembrandId": 5001,
        "hstnumIssueQty": 10.0,
        "hststrBatchSlNo": "BATCH001"
        // ... other item fields
      }
    ]
    // ... other issue fields
  }
}
```

### Error Response (Not Found)
**Status**: `404 Not Found`
```json
{
  "timestamp": "2026-01-13T10:00:00.000+00:00",
  "message": "Issue not found with Issue No: 9999",
  "details": "uri=/api/issue/details"
}
```

## 3. Get Patient Issue History
Retrieves all issues associated with a specific patient CR Number (PUK).

- **URL**: `/patient`
- **Method**: `GET`

### Query Parameters
| Parameter | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `hospitalCode` | Integer | Yes | The hospital code |
| `crNo` | String | Yes | The Patient CR Number (PUK) |

### Success Response (`ApiResponse<List<IssueResponseDto>>`)
**Status**: `200 OK`
```json
{
  "success": true,
  "message": "Patient issue history fetched successfully",
  "data": [
    {
      "hstnumIssueNo": 1005,
      "hrgnumPuk": "101010101",
      "items": [...]
    },
    {
      "hstnumIssueNo": 1002,
      "hrgnumPuk": "101010101",
      "items": [...]
    }
  ]
}
```

## Error Handling
Global exceptions are handled and return the following structure:

**Status**: `500 Internal Server Error`
```json
{
  "timestamp": "2026-01-13T10:00:00.000+00:00",
  "message": "Internal Server Error: [Exception Message]",
  "details": "uri=[Request URI]"
}
```
