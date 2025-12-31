# Mastering Advanced Microservices (MAMS) - Saga Choreography

A multi-module Maven project demonstrating the **Saga Choreography Pattern** for distributed transactions using Spring Boot, Spring Cloud Stream, and Kafka.

## 🚀 Service Architecture & Ports

| Service | Port | Description |
| :--- | :--- | :--- |
| **Booking Service** | 8081 | Orchestrator/Initiator (Saga Start) |
| **Seat Inventory Service** | 8082 | Manages Seat Availability & Locking |
| **Payment Service** | 8083 | Manages User Balance & Payments |
| **Kafka** | 9092 | Message Broker |
| **Postgres** | 5432 | Database (Shared for demo: `booking_database`) |

## 🏗️ Build & Run

### Prerequisites
- Java 17
- Maven
- PostgreSQL (Port 5432, user: `postgres`, pass: `1234`)

### Steps
1.  **Start Infrastructure**:
    Run the provided script to build projects and start Kafka, Zookeeper, and all Microservices.
    ```powershell
    run_all_with_kafka.bat
    ```

## 🛠️ API Documentation

### **1. Booking Service** (Port 8081)
**Create Booking (Saga Start)**
- **Endpoint**: `POST /bookings`
- **Description**: Initiates a booking, locks seats, and processes payment.
- **Request**:
  ```json
  {
    "userId": "101",
    "showId": "101",
    "seatIds": ["1", "2"],
    "amount": 100
  }
  ```
- **Response**: `200 OK` with Status `PENDING` (Final status updated asynchronously).

**Get Booking Status**
- **Endpoint**: `GET /bookings/{bookingId}`
- **Response**:
  ```json
  {
    "reservationId": "UUID",
    "userId": "101",
    "status": "CONFIRMED"
  }
  ```

**Get All Bookings**
- **Endpoint**: `GET /bookings`

### **2. Seat Inventory Service** (Port 8082)
Manages seat inventory.

**CRUD Endpoints**
- **Add Seat**: `POST /inventory`
  ```json
  { "seatId": 1, "showId": "101", "status": "AVAILABLE" }
  ```
- **Get All Seats**: `GET /inventory`
- **Get Seats by Show**: `GET /inventory/show/{showId}`
- **Update Status**: `PUT /inventory/{id}/status?status=LOCKED`
- **Delete Seat**: `DELETE /inventory/{id}`

### **3. Payment Service** (Port 8083)
Event-driven service. Reacts to `seat-reserved-events` to process payments.

**Endpoints**
- **Get All Balances**: `GET /payments`
- **Get Balance by User**: `GET /payments/{userId}`

## 🔄 Saga Flow (Choreography)

![Saga Diagram](images/Saga%20Choreography%20in%20Microservices%20🔥%20Movie%20Ticket%20Booking%20System%20Explained.png)

1.  **Booking Service**: Creates Booking (`PENDING`) -> Emits `BookingCreatedEvent`.
2.  **Seat Inventory**: Consumes Event -> Locks Seats -> Emits `SeatReservedEvent` (Success/Fail).
3.  **Payment Service**: Consumes `SeatReservedEvent` -> Deducts Balance -> Emits `BookingPaymentEvent` (Success/Fail).
4.  **Completion**:
    - **Booking Service**: Updates status to `CONFIRMED` or `FAILED`.
    - **Seat Inventory**: Updates status to `BOOKED` or `AVAILABLE` (Release).

### 🧩 Deep Dive: Transaction Flow & Events

This section details the event-driven interaction for a successful booking.

#### **Step 1: Booking Initiated**
- **Service**: `Booking Service`
- **Action**: User creates a booking.
- **State**: `PENDING`
- **Topic**: `booking-created-events`
- **Event Payload** (`BookingCreatedEvent`):
  ```json
  {
    "bookingId": "c4d3-...",
    "userId": "101",
    "showId": "101",
    "seatIds": ["1", "2"],
    "amount": 100
  }
  ```

#### **Step 2: Seat Reservation**
- **Service**: `Seat Inventory Service`
- **Action**: Consumes `BookingCreatedEvent`. Checks availability and locks seats.
- **State**: Seats set to `LOCKED`.
- **Topic**: `seat-reserved-events`
- **Event Payload** (`SeatReservedEvent`):
  ```json
  {
    "bookingId": "c4d3-...",
    "userId": "101",
    "reserved": true,
    "amount": 100
  }
  ```
  *(If locking fails, `reserved` is `false`)*

#### **Step 3: Payment Processing**
- **Service**: `Payment Service`
- **Action**: Consumes `SeatReservedEvent`. Deducts user balance.
- **Topic**: `booking-payment-events`
- **Event Payload** (`BookingPaymentEvent`):
  ```json
  {
    "bookingId": "c4d3-...",
    "paymentCompleted": true,
    "amount": 100
  }
  ```
  *(If balance insufficient, `paymentCompleted` is `false`)*

#### **Step 4: Finalization**
- **Service**: `Booking Service` & `Seat Inventory Service`
- **Action**: Both consume `BookingPaymentEvent`.
    - **Booking Service**: Updates status to `CONFIRMED` (if payment success) or `FAILED`.
    - **Seat Inventory Service**: Updates seats to `BOOKED` (if payment success) or releases them to `AVAILABLE`.

## 🧪 Testing Scenarios

1.  **Happy Path**: User with sufficient balance books available seats. -> **CONFIRMED**, **BOOKED**.
2.  **Insufficient Funds**: User with low balance. -> **FAILED**, Seats Released to **AVAILABLE**.
3.  **Seat Unavailable**: User tries to book locked/booked seats. -> **FAILED**.