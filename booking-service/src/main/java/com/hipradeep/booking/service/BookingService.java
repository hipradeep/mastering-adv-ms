package com.hipradeep.booking.service;

import com.hipradeep.booking.entity.Booking;
import com.hipradeep.booking.repository.BookingRepository;
import com.hipradeep.saga.commons.dto.BookingRequest;
import com.hipradeep.saga.commons.dto.BookingResponse;
import com.hipradeep.saga.commons.event.BookingCreatedEvent;
import com.hipradeep.saga.commons.event.BookingPaymentEvent;
import com.hipradeep.saga.commons.event.SeatReservedEvent;
import com.hipradeep.saga.commons.KafkaConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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

        kafkaTemplate.send(KafkaConfigProperties.BOOKING_CREATED_TOPIC, event);
        log.info("Booking created and event published: {}", event);

        return new BookingResponse(bookingId, bookingRequest.userId(), bookingRequest.showId(), bookingRequest.amount(), "PENDING");
    }

    // Listener for Payment Events
    @KafkaListener(topics = KafkaConfigProperties.PAYMENT_PROCESSED_TOPIC, groupId = KafkaConfigProperties.BOOKING_GROUP_ID)
    public void handlePaymentEvent(BookingPaymentEvent event) {
        log.info("BookingService: Received Payment Event: {}", event);
        bookingRepository.findByBookingId(event.bookingId()).ifPresent(booking -> {
            if (event.paymentCompleted()) {
                booking.setStatus("CONFIRMED");
            } else {
                booking.setStatus("FAILED");
            }
            bookingRepository.save(booking);
            log.info("BookingService: Updated status to {} for bookingId {}", booking.getStatus(), event.bookingId());
        });
    }

    @KafkaListener(topics = KafkaConfigProperties.SEAT_RESERVED_TOPIC, groupId = KafkaConfigProperties.BOOKING_GROUP_ID)
    public void handleSeatReservedEvent(SeatReservedEvent event) {
        if (!event.reserved()) {
            log.info("BookingService: Received Seat Reservation Failure for bookingId {}", event.bookingId());
            bookingRepository.findByBookingId(event.bookingId()).ifPresent(booking -> {
                booking.setStatus("FAILED");
                bookingRepository.save(booking);
                log.info("BookingService: Updated status to FAILED due to seat unavailability for bookingId {}", event.bookingId());
            });
        }
    }
}
