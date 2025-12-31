package com.hipradeep.payment.consumer;

import com.hipradeep.payment.repository.UserBalanceRepository;
import com.hipradeep.payment.producer.PaymentProducer;
import com.hipradeep.saga.commons.KafkaConfigProperties;
import com.hipradeep.saga.commons.event.SeatReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventListener {

    private final UserBalanceRepository userBalanceRepository;
    private final PaymentProducer paymentProducer;

    /**
     * Listen for Seat Reservation Events to initiate payment.
     * Topic: seat-reserved-events (KafkaConfigProperties.SEAT_RESERVED_TOPIC)
     * Action:
     *  - Verify seat reserved (reserved=true).
     *  - Deduct Amount from User Balance.
     *  - Publish BookingPaymentEvent (success=true/false).
     */
    @Transactional
    @KafkaListener(topics = KafkaConfigProperties.SEAT_RESERVED_TOPIC, groupId = KafkaConfigProperties.PAYMENT_GROUP_ID)
    public void processPayment(SeatReservedEvent event) {
        log.info("PaymentEventListener: Received SeatReservedEvent: {}", event);

        if (!event.reserved()) {
            log.info("PaymentEventListener: Seat reservation failed for bookingId {}, skipping payment.", event.bookingId());
            return;
        }

        // Check Balance using userId from event
        int userId = Integer.parseInt(event.userId());

        userBalanceRepository.findById(userId).ifPresentOrElse(balance -> {
            if (balance.getPrice() >= event.amount()) {
                balance.setPrice(balance.getPrice() - event.amount());
                userBalanceRepository.save(balance);
                
                paymentProducer.publishBookingPaymentEvent(event.bookingId(), true, event.amount());
                log.info("PaymentEventListener: Payment successful for bookingId {}", event.bookingId());
            } else {
                paymentProducer.publishBookingPaymentEvent(event.bookingId(), false, event.amount());
                log.error("PaymentEventListener: Insufficient balance for bookingId {}", event.bookingId());
            }
        }, () -> {
            paymentProducer.publishBookingPaymentEvent(event.bookingId(), false, event.amount());
            log.error("PaymentEventListener: User {} not found for bookingId {}", userId, event.bookingId());
        });
    }
}
