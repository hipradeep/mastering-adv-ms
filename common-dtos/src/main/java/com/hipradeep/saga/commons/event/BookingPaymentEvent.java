package com.hipradeep.saga.commons.event;

import java.util.List;

public record BookingPaymentEvent(String bookingId, String userId, String showId, List<String> seatIds,
                boolean paymentCompleted,
                long amount) {
}
