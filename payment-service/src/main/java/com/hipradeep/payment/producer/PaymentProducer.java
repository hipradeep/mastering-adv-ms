package com.hipradeep.payment.producer;

import com.hipradeep.saga.commons.KafkaConfigProperties;
import com.hipradeep.saga.commons.event.BookingPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBookingPaymentEvent(String bookingId, String userId, String showId, java.util.List<String> seatIds, boolean paymentCompleted, long amount) {
        BookingPaymentEvent paymentEvent = new BookingPaymentEvent(bookingId, userId, showId, seatIds, paymentCompleted, amount);
        kafkaTemplate.send(KafkaConfigProperties.PAYMENT_EVENTS_TOPIC, bookingId, paymentEvent);
        log.info("PaymentProducer: Published BookingPaymentEvent: {}", paymentEvent);
    }
}
