package com.hipradeep.saga.commons;

public class KafkaConfigProperties {

    /*
     * Saga Orchestration Flow:
     * 1. Orchestrator -> Booking Service (MOVIE_BOOKING_EVENTS_TOPIC) [PENDING]
     * 2. Orchestrator -> Seat Inventory (SEAT_RESERVED_CMD_TOPIC) [Command: Reserve]
     * 3. Seat Inventory -> Orchestrator (SEAT_RESERVED_TOPIC) [Reply: Success/Fail]
     * 4. Orchestrator -> Payment Service (PAYMENT_EVENTS_CMD_TOPIC) [Command: Pay]
     * 5. Payment Service -> Orchestrator (PAYMENT_EVENTS_TOPIC) [Reply:Success/Fail]
     * 6. Orchestrator -> Booking Service (MOVIE_BOOKING_EVENTS_TOPIC) [CONFIRMED/FAILED]
     * 7. (On Fail) Orchestrator -> Seat Inventory (SEAT_RESERVED_CMD_TOPIC) [Command: Release]
     */

    public static final String BOOKING_CREATED_TOPIC = "booking-created-events";
    public static final String MOVIE_BOOKING_EVENTS_TOPIC = "booking-created-events";
    public static final String SEAT_RESERVED_TOPIC = "seat-reserved-events";
    public static final String SEAT_RESERVED_CMD_TOPIC = "seat-reserved-commands";
    public static final String PAYMENT_EVENTS_TOPIC = "booking-payment-events";
    public static final String PAYMENT_EVENTS_CMD_TOPIC = "booking-payment-commands";

    public static final String BOOKING_GROUP_ID = "booking-group";
    public static final String MOVIE_BOOKING_GROUP = "booking-group";
    public static final String PAYMENT_GROUP_ID = "payment-group";
    public static final String INVENTORY_GROUP_ID = "inventory-group";
    public static final String ORCHESTRATOR_CONSUMER_GROUP = "orchestrator-group";
}
