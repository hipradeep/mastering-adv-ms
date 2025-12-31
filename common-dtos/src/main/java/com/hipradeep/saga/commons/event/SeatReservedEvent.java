package com.hipradeep.saga.commons.event;

import java.util.List;

public record SeatReservedEvent(String bookingId, String userId, String showId, List<String> seatIds, boolean reserved,
        long amount) {
}
