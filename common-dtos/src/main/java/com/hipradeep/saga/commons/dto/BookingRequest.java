package com.hipradeep.saga.commons.dto;

import java.util.List;

public record BookingRequest(String userId, String showId, List<String> seatIds, long amount) {
}
