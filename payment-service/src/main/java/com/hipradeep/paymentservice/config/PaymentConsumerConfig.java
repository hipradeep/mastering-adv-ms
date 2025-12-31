package com.hipradeep.paymentservice.config;

import com.hipradeep.paymentservice.service.PaymentService;
import com.hipradeep.saga.commons.event.InventoryEvent;
import com.hipradeep.saga.commons.event.InventoryStatus;
import com.hipradeep.saga.commons.event.OrderEvent;
import com.hipradeep.saga.commons.event.OrderStatus;
import com.hipradeep.saga.commons.event.PaymentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
@Slf4j
public class PaymentConsumerConfig {

    @Autowired
    private PaymentService paymentService;

    @Bean
    public Function<OrderEvent, PaymentEvent> paymentProcessor() {
        return orderEvent -> {
            log.info("Payment Processor received OrderEvent: {}", orderEvent);
            PaymentEvent paymentEvent = processPayment(orderEvent);
            log.info("Payment Processor emitting PaymentEvent: {}", paymentEvent);
            return paymentEvent;
        };
    }

    private PaymentEvent processPayment(OrderEvent orderEvent) {
        // if ORDER_CREATED -> process payment
        // if ORDER_CANCELLED -> (maybe refund)
        if (OrderStatus.ORDER_CREATED.equals(orderEvent.getOrderStatus())) {
            return paymentService.newOrderEvent(orderEvent);
        } else {
            paymentService.cancelOrderEvent(orderEvent);
            return new PaymentEvent();
        }
    }

    // @Bean
    // public Consumer<InventoryEvent> inventoryEventConsumer() {
    // return inventory -> {
    // if (InventoryStatus.INVENTORY_FAILED.equals(inventory.getInventoryStatus()))
    // {
    // paymentService.cancelOrderEvent(inventory);
    // }
    // };
    // }
}
