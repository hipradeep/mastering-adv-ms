package com.hipradeep.payment.service;

import com.hipradeep.payment.entity.UserBalance;
import com.hipradeep.payment.repository.UserBalanceRepository;
import com.hipradeep.saga.commons.event.BookingPaymentEvent;
import com.hipradeep.saga.commons.event.SeatReservedEvent;
import com.hipradeep.saga.commons.KafkaConfigProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final UserBalanceRepository userBalanceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostConstruct
    public void initUserBalanceInDb() {
        if(userBalanceRepository.count()==0) {
            userBalanceRepository.saveAll(Stream.of(
                    new UserBalance(101, 5000),
                    new UserBalance(102, 3000),
                    new UserBalance(103, 4200),
                    new UserBalance(104, 20000),
                    new UserBalance(105, 999)
            ).collect(Collectors.toList()));
        }
    }

    public java.util.List<UserBalance> getAllUserBalances() {
        return userBalanceRepository.findAll();
    }

    public UserBalance getUserBalance(int userId) {
        return userBalanceRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Transactional
    @KafkaListener(topics = KafkaConfigProperties.SEAT_RESERVED_TOPIC, groupId = KafkaConfigProperties.PAYMENT_GROUP_ID)
    public void processPayment(SeatReservedEvent event) {
        log.info("PaymentService: Received SeatReservedEvent: {}", event);

        if (!event.reserved()) {
            log.info("PaymentService: Seat reservation failed for bookingId {}, skipping payment.", event.bookingId());
            return;
        }

        // Check Balance using userId from event
        int userId = Integer.parseInt(event.userId());

        userBalanceRepository.findById(userId).ifPresentOrElse(balance -> {
            if (balance.getPrice() >= event.amount()) {
                balance.setPrice(balance.getPrice() - event.amount());
                userBalanceRepository.save(balance);
                
                BookingPaymentEvent paymentEvent = new BookingPaymentEvent(event.bookingId(), true, event.amount());
                kafkaTemplate.send(KafkaConfigProperties.PAYMENT_PROCESSED_TOPIC, paymentEvent);
                log.info("PaymentService: Payment successful for bookingId {}", event.bookingId());
            } else {
                BookingPaymentEvent paymentEvent = new BookingPaymentEvent(event.bookingId(), false, event.amount());
                kafkaTemplate.send(KafkaConfigProperties.PAYMENT_PROCESSED_TOPIC, paymentEvent);
                log.error("PaymentService: Insufficient balance for bookingId {}", event.bookingId());
            }
        }, () -> {
            BookingPaymentEvent paymentEvent = new BookingPaymentEvent(event.bookingId(), false, event.amount());
            kafkaTemplate.send(KafkaConfigProperties.PAYMENT_PROCESSED_TOPIC, paymentEvent);
            log.error("PaymentService: User {} not found for bookingId {}", userId, event.bookingId());
        });
    }
}
