package com.hipradeep.booking.controller;

import com.hipradeep.booking.service.BookingService;
import com.hipradeep.saga.commons.dto.BookingRequest;
import com.hipradeep.saga.commons.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;



    @org.springframework.web.bind.annotation.GetMapping
    public java.util.List<com.hipradeep.booking.entity.Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @org.springframework.web.bind.annotation.GetMapping("/{bookingId}")
    public BookingResponse getBookingStatus(@org.springframework.web.bind.annotation.PathVariable String bookingId) {
        return bookingService.getBookingStatus(bookingId);
    }
}
