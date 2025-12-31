package com.hipradeep.booking.consumer;

import com.hipradeep.booking.repository.BookingRepository;
import com.hipradeep.saga.commons.KafkaConfigProperties;
import com.hipradeep.saga.commons.event.BookingPaymentEvent;
import com.hipradeep.saga.commons.event.SeatReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingEventListener {

    private final BookingRepository bookingRepository;

    /**
     * Listen for Payment Events to update booking status.
     * Topic: booking-payment-events (KafkaConfigProperties.PAYMENT_PROCESSED_TOPIC)
     * Action:
     *  - If payment success -> status = CONFIRMED
     *  - If payment failed -> status = FAILED
     */
    @KafkaListener(topics = KafkaConfigProperties.PAYMENT_PROCESSED_TOPIC, groupId = KafkaConfigProperties.BOOKING_GROUP_ID)
    public void handlePaymentEvent(BookingPaymentEvent event) {
        log.info("BookingEventListener: Received Payment Event: {}", event);
        bookingRepository.findByBookingId(event.bookingId()).ifPresent(booking -> {
            if (event.paymentCompleted()) {
                booking.setStatus("CONFIRMED");
            } else {
                booking.setStatus("FAILED");
            }
            bookingRepository.save(booking);
            log.info("BookingEventListener: Updated status to {} for bookingId {}", booking.getStatus(), event.bookingId());
        });
    }

    /**
     * Listen for Seat Reservation Failure Events.
     * Topic: seat-reserved-events (KafkaConfigProperties.SEAT_RESERVED_TOPIC)
     * Action:
     *  - If seat reservation failed (reserved=false) -> status = FAILED
     *  - If success, we wait for Payment Event to confirm.
     */
    @KafkaListener(topics = KafkaConfigProperties.SEAT_RESERVED_TOPIC, groupId = KafkaConfigProperties.BOOKING_GROUP_ID)
    public void handleSeatReservedEvent(SeatReservedEvent event) {
        if (!event.reserved()) {
            log.info("BookingEventListener: Received Seat Reservation Failure for bookingId {}", event.bookingId());
            bookingRepository.findByBookingId(event.bookingId()).ifPresent(booking -> {
                booking.setStatus("FAILED");
                bookingRepository.save(booking);
                log.info("BookingEventListener: Updated status to FAILED due to seat unavailability for bookingId {}", event.bookingId());
            });
        }
    }
}
