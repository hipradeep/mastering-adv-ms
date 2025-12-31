package com.hipradeep.saga.commons.event;

public record SeatReservedEvent(String bookingId, String userId, boolean reserved, long amount) {
}
