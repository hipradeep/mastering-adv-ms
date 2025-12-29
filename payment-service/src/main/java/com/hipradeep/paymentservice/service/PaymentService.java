package com.hipradeep.paymentservice.service;

import com.hipradeep.paymentservice.entity.UserBalance;
import com.hipradeep.paymentservice.entity.UserTransaction;
import com.hipradeep.paymentservice.repository.UserBalanceRepository;
import com.hipradeep.paymentservice.repository.UserTransactionRepository;
import com.hipradeep.saga.commons.dto.OrderRequestDto;
import com.hipradeep.saga.commons.dto.PaymentRequestDto;
import com.hipradeep.saga.commons.event.InventoryEvent;
import com.hipradeep.saga.commons.event.OrderEvent;
import com.hipradeep.saga.commons.event.PaymentEvent;
import com.hipradeep.saga.commons.event.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.stream.Stream;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserTransactionRepository userTransactionRepository;

    @PostConstruct
    public void initUserBalanceInDB() {
        userBalanceRepository.saveAll(Stream.of(new UserBalance(101, 5000),
                new UserBalance(102, 3000),
                new UserBalance(103, 4200),
                new UserBalance(104, 20000),
                new UserBalance(105, 999),
                new UserBalance(1, 1000)).toList());
    }

    @Transactional
    public PaymentEvent newOrderEvent(OrderEvent orderEvent) {
        log.info("Received OrderEvent in PaymentService: {}", orderEvent);
        OrderRequestDto orderRequestDto = orderEvent.getOrderRequestDto();

        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                orderRequestDto.getOrderId(),
                orderRequestDto.getUserId(),
                orderRequestDto.getAmount(),
                orderRequestDto.getProductId());

        return userBalanceRepository.findById(orderRequestDto.getUserId())
                .filter(ub -> {
                    boolean hasBalance = ub.getPrice() >= orderRequestDto.getAmount();
                    if (!hasBalance) {
                        log.warn("Insufficient balance for userId: {}. Required: {}, Available: {}",
                                orderRequestDto.getUserId(), orderRequestDto.getAmount(), ub.getPrice());
                    }
                    return hasBalance;
                })
                .map(ub -> {
                    log.info("Processing payment for userId: {} and orderId: {}", orderRequestDto.getUserId(),
                            orderRequestDto.getOrderId());
                    ub.setPrice(ub.getPrice() - orderRequestDto.getAmount());
                    userTransactionRepository.save(new UserTransaction(
                            orderRequestDto.getOrderId(),
                            orderRequestDto.getUserId(),
                            orderRequestDto.getAmount()));
                    log.info("Payment successful for orderId: {}", orderRequestDto.getOrderId());
                    return new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_COMPLETED);
                }).orElseGet(() -> {
                    log.error("Payment failed for orderId: {} - User not found or insufficient balance",
                            orderRequestDto.getOrderId());
                    return new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_FAILED);
                });
    }

    @Transactional
    public void cancelOrderEvent(OrderEvent orderEvent) {
        log.info("Received Order Cancellation (from Order Service) in PaymentService: {}", orderEvent);
        // Logic to refund if needed, but here we likely won't call this for simple
        // failures
        // unless we need to compensate a previous deduction.
        // For simple flow: if payment failed, order is cancelled.
        // If Inventory fails later, we might need to credit back.
    }

    @Transactional
    public void cancelOrderEvent(InventoryEvent inventoryEvent) {
        log.info("Received Inventory Failure (for refund) in PaymentService: {}", inventoryEvent);
        // Refund logic
        // Find user, credit back amount
        // Assuming we assume the transaction happened if we are here (because we only
        // listen to Inventory if Payment succeeded, via flow?)
        // Wait, Payment emits success, Inventory listens. If Inventory fails, it emits
        // Fail.
        // We listen to Fail.
        // So yes, we should refund.
        OrderRequestDto dto = inventoryEvent.getOrderRequestDto();
        userBalanceRepository.findById(dto.getUserId()).ifPresent(ub -> {
            ub.setPrice(ub.getPrice() + dto.getAmount());
            userBalanceRepository.save(ub);
        });
        // Also maybe delete UserTransaction or mark as refunded?
    }
}
