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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class PaymentConsumerConfig {

    @Autowired
    private PaymentService paymentService;

    @Bean
    public Function<Flux<OrderEvent>, Flux<PaymentEvent>> paymentProcessor() {
        return orderEventFlux -> orderEventFlux.flatMap(this::processPayment);
    }

    private Mono<PaymentEvent> processPayment(OrderEvent orderEvent) {
        // if ORDER_CREATED -> process payment
        // if ORDER_CANCELLED -> (maybe refund)
        if (OrderStatus.ORDER_CREATED.equals(orderEvent.getOrderStatus())) {
            return Mono.fromSupplier(() -> paymentService.newOrderEvent(orderEvent));
        } else {
            return Mono.fromRunnable(() -> paymentService.cancelOrderEvent(orderEvent));
        }
    }

    @Bean
    public Consumer<InventoryEvent> inventoryEventConsumer() {
        return inventory -> {
            if (InventoryStatus.INVENTORY_FAILED.equals(inventory.getInventoryStatus())) {
                paymentService.cancelOrderEvent(inventory);
            }
        };
    }
}
