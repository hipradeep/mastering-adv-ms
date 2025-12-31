package com.hipradeep.booking.service;

import com.hipradeep.booking.entity.Booking;
import com.hipradeep.booking.producer.BookingProducer;
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
    private final BookingProducer bookingProducer;

    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setUserId(bookingRequest.userId());
        booking.setShowId(bookingRequest.showId());
        booking.setSeatIds(String.join(",", bookingRequest.seatIds()));
        booking.setAmount(bookingRequest.amount());
        booking.setStatus("PENDING");

        bookingRepository.save(booking);

        BookingCreatedEvent event = new BookingCreatedEvent(
                bookingId,
                bookingRequest.userId(),
                bookingRequest.showId(),
                bookingRequest.seatIds(),
                bookingRequest.amount()
        );

        bookingProducer.publishBookingCreatedEvent(event);

        return new BookingResponse(bookingId, bookingRequest.userId(), bookingRequest.showId(), bookingRequest.amount(), "PENDING");
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
