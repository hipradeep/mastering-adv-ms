package com.hipradeep.saga.commons.event;

public record BookingPaymentEvent(String bookingId, boolean paymentCompleted, long amount) {
}
