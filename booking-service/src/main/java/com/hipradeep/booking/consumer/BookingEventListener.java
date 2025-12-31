package com.hipradeep.booking.consumer;

import com.hipradeep.booking.service.BookingService;
import com.hipradeep.saga.commons.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.hipradeep.saga.commons.KafkaConfigProperties.MOVIE_BOOKING_EVENTS_TOPIC;
import static com.hipradeep.saga.commons.KafkaConfigProperties.MOVIE_BOOKING_GROUP;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingEventListener {

    private final BookingService bookingService;

    @KafkaListener(topics = MOVIE_BOOKING_EVENTS_TOPIC, groupId = MOVIE_BOOKING_GROUP)
    public void consumeBookingEvent(BookingCreatedEvent event) {
        log.info("Consuming booking event for id: {} with status: {}", event.bookingId(), event.status());
        bookingService.processBooking(event);
    }
}
