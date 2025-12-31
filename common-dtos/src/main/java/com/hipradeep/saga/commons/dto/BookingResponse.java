package com.hipradeep.saga.commons.dto;

public record BookingResponse(String reservationId, String userId, String showId, long amount, String status) {
}
