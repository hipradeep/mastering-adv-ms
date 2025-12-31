package com.hipradeep.payment.service;

import com.hipradeep.payment.entity.Payment;
import com.hipradeep.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final PaymentRepository paymentRepository;

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void listenOrderEvents(String message) {
        System.out.println("Payment Service Received Order Event: " + message);

        // Simulating parsing the message. In a real app, message would be JSON.
        // Assuming message contains Order ID or we generate a payment for the order.
        Payment payment = new Payment();
        payment.setOrderId(message); // Storing the raw message as Order ID for simplicity or parsing if needed
        payment.setAmount(100.0); // Dummy amount
        payment.setStatus("SUCCESS");
        payment.setPaymentDate(LocalDateTime.now());

        paymentRepository.save(payment);
        System.out.println("Payment processed and saved for Order: " + message);
    }

    @KafkaListener(topics = "user-events", groupId = "payment-group")
    public void listenUserEvents(String message) {
        System.out.println("Payment Service Received User Event: " + message);
        // Just logging as per notification service pattern
    }
}
