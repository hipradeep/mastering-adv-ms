package com.hipradeep.saga.commons;

public class KafkaConfigProperties {
    public static final String BOOKING_CREATED_TOPIC = "booking-created-events";
    public static final String SEAT_RESERVED_TOPIC = "seat-reserved-events";
    public static final String PAYMENT_PROCESSED_TOPIC = "booking-payment-events";

    public static final String BOOKING_GROUP_ID = "booking-group";
    public static final String PAYMENT_GROUP_ID = "payment-group";
    public static final String INVENTORY_GROUP_ID = "inventory-group";
}
