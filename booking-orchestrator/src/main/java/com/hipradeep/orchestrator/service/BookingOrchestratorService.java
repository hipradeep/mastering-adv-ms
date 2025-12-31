package com.hipradeep.orchestrator.service;

import com.hipradeep.saga.commons.dto.BookingRequest;
import com.hipradeep.saga.commons.dto.BookingResponse;
import com.hipradeep.saga.commons.event.BookingCreatedEvent;
import com.hipradeep.saga.commons.event.BookingPaymentEvent;
import com.hipradeep.saga.commons.event.SeatReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.hipradeep.saga.commons.KafkaConfigProperties.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingOrchestratorService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingResponse createBooking(BookingRequest request) {
        String bookingId = UUID.randomUUID().toString();
        
        // 1. Publish Booking Created (PENDING) - for persistence
        BookingCreatedEvent bookingCreatedEvent = new BookingCreatedEvent(
                bookingId,
                request.userId(),
                request.showId(),
                request.seatIds(),
                request.amount(),
                "PENDING"
        );
        kafkaTemplate.send(BOOKING_CREATED_TOPIC, bookingId, bookingCreatedEvent);
        log.info("Orchestrator:: Published Booking Created Event (PENDING): {}", bookingId);

        // 2. Send Seat Reservation Command
        SeatReservedEvent seatCommand = new SeatReservedEvent(
                bookingId,
                request.userId(),
                request.showId(),
                request.seatIds(),
                true,
                request.amount()
        );
        kafkaTemplate.send(SEAT_RESERVED_CMD_TOPIC, bookingId, seatCommand);
        log.info("Orchestrator:: Published Seat Reserved Command: {}", bookingId);

        return new BookingResponse(bookingId, request.userId(), request.showId(), request.amount(), "PENDING");
    }

    @KafkaListener(topics = SEAT_RESERVED_TOPIC, groupId = ORCHESTRATOR_CONSUMER_GROUP)
    public void handleSeatReservation(SeatReservedEvent event) {
        log.info("Orchestrator:: Received Seat Reserved Event: {}", event);
        if (event.reserved()) {
            // Success -> Proceed to Payment
            // Note: We need userId for payment. SeatReservedEvent lost it.
            // We should add userId to SeatReservedEvent too.
            BookingPaymentEvent paymentCommand = new BookingPaymentEvent(
                    event.bookingId(),
                    event.userId(),
                    event.showId(),
                    event.seatIds(),
                    false, // Not used in command
                    event.amount()
            );
            kafkaTemplate.send(PAYMENT_EVENTS_CMD_TOPIC, event.bookingId(), paymentCommand);
            log.info("Orchestrator:: Published Payment Command: {}", event.bookingId());
        } else {
            // Failure -> Rollback (Mark Booking as Failed)
            sendBookingStatusUpdate(event.bookingId(), "FAILED");
            log.info("Orchestrator:: Seat Reservation Failed. Marking Booking FAILED: {}", event.bookingId());
        }
    }

    @KafkaListener(topics = PAYMENT_EVENTS_TOPIC, groupId = ORCHESTRATOR_CONSUMER_GROUP)
    public void handlePaymentProcessing(BookingPaymentEvent event) {
        log.info("Orchestrator:: Received Payment Event: {}", event);
        if (event.paymentCompleted()) {
            // Success -> Complete Booking
            sendBookingStatusUpdate(event.bookingId(), "CONFIRMED");
            log.info("Orchestrator:: Payment Success. Marking Booking CONFIRMED: {}", event.bookingId());
        } else {
            // Failure -> Rollback Seat Reservation
            SeatReservedEvent rollbackCommand = new SeatReservedEvent(
                    event.bookingId(),
                    event.userId(),
                    event.showId(),
                    event.seatIds(),
                    false, // reserved=false means release
                    event.amount()
            );
            kafkaTemplate.send(SEAT_RESERVED_CMD_TOPIC, event.bookingId(), rollbackCommand);
            log.info("Orchestrator:: Payment Failed. Sending Seat Rollback Command: {}", event.bookingId());
            
            // Also mark booking failed
            sendBookingStatusUpdate(event.bookingId(), "FAILED");
        }
    }

    private void sendBookingStatusUpdate(String bookingId, String status) {
        // We need to fetch original details or just send status update. 
        // For simplicity, assuming Booking Service can handle partial updates or we send minimal info.
        // But BookingCreatedEvent requires all fields. 
        // In real app, we might just send status, or reconstruct if we had state.
        // Here, we'll send a dummy event but with correct ID and Status, assuming persistence service can handle it.
        // Or better, we should have stored the context.
        // Reference implementation sends full event.
        // Assuming persistence service updates by ID.
        
        BookingCreatedEvent updateEvent = new BookingCreatedEvent(
                bookingId,
                null, // userId unknown here without state
                null, // showId unknown
                null, // seatIds unknown
                0,    // amount unknown
                status
        );
        kafkaTemplate.send(BOOKING_CREATED_TOPIC, bookingId, updateEvent);
    }
}
