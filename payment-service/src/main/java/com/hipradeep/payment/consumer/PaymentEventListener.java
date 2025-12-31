package com.hipradeep.payment.consumer;

import com.hipradeep.payment.repository.UserBalanceRepository;
import com.hipradeep.payment.producer.PaymentProducer;
import com.hipradeep.saga.commons.KafkaConfigProperties;

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

    @Transactional
    @KafkaListener(topics = KafkaConfigProperties.PAYMENT_EVENTS_CMD_TOPIC, groupId = KafkaConfigProperties.PAYMENT_GROUP_ID)
    public void processPayment(com.hipradeep.saga.commons.event.BookingPaymentEvent event) {
        log.info("PaymentEventListener: Received BookingPaymentEvent Command: {}", event);

        // Check Balance using userId from event
        if(event.userId() == null) {
             log.error("PaymentEventListener: userId is null for bookingId {}", event.bookingId());
             paymentProducer.publishBookingPaymentEvent(event.bookingId(), event.userId(), event.showId(), event.seatIds(), false, event.amount());
             return;
        }

        int userId = Integer.parseInt(event.userId());

        userBalanceRepository.findById(userId).ifPresentOrElse(balance -> {
            if (balance.getPrice() >= event.amount()) {
                balance.setPrice(balance.getPrice() - event.amount());
                userBalanceRepository.save(balance);
                
                paymentProducer.publishBookingPaymentEvent(event.bookingId(), event.userId(), event.showId(), event.seatIds(), true, event.amount());
                log.info("PaymentEventListener: Payment successful for bookingId {}", event.bookingId());
            } else {
                paymentProducer.publishBookingPaymentEvent(event.bookingId(), event.userId(), event.showId(), event.seatIds(), false, event.amount());
                log.error("PaymentEventListener: Insufficient balance for bookingId {}", event.bookingId());
            }
        }, () -> {
            paymentProducer.publishBookingPaymentEvent(event.bookingId(), event.userId(), event.showId(), event.seatIds(), false, event.amount());
            log.error("PaymentEventListener: User {} not found for bookingId {}", userId, event.bookingId());
        });
    }
}
