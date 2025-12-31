package com.hipradeep.booking.service;

import com.hipradeep.booking.entity.Booking;

import com.hipradeep.booking.repository.BookingRepository;
import com.hipradeep.saga.commons.dto.BookingRequest;
import com.hipradeep.saga.commons.dto.BookingResponse;
import com.hipradeep.saga.commons.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    public void processBooking(BookingCreatedEvent event) {
        log.info("Processing booking event for id: {}", event.bookingId());
        Booking booking = bookingRepository.findByBookingId(event.bookingId())
                .orElse(new Booking());
        
        if (booking.getBookingId() == null) {
            booking.setBookingId(event.bookingId());
            booking.setUserId(event.userId());
            booking.setShowId(event.showId());
            if (event.seatIds() != null) {
                booking.setSeatIds(String.join(",", event.seatIds()));
            }
            booking.setAmount(event.amount());
        }
        booking.setStatus(event.status());
        bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public BookingResponse getBookingStatus(String bookingId) {
        return bookingRepository.findByBookingId(bookingId)
                .map(booking -> new BookingResponse(
                        booking.getBookingId(),
                        booking.getUserId(),
                        booking.getShowId(),
                        booking.getAmount(),
                        booking.getStatus()
                ))
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
    }
}
