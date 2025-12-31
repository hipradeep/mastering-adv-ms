package com.hipradeep.orderservice.config;

import com.hipradeep.orderservice.repository.OrderRepository;
import com.hipradeep.saga.commons.event.InventoryEvent;
import com.hipradeep.saga.commons.event.InventoryStatus;
import com.hipradeep.saga.commons.event.OrderStatus;
import com.hipradeep.saga.commons.event.PaymentEvent;
import com.hipradeep.saga.commons.event.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class EventConsumerConfig {

    @Autowired
    private OrderRepository orderRepository;

    @Bean
    public Consumer<PaymentEvent> paymentEventConsumer() {
        return (payment) -> {
            log.info("Order Service received PaymentEvent: {}", payment);
            orderRepository.findById(payment.getPaymentRequestDto().getOrderId()).ifPresent(order -> {
                order.setPaymentStatus(payment.getPaymentStatus());
                if (PaymentStatus.PAYMENT_FAILED.equals(payment.getPaymentStatus())) {
                    log.error("Payment failed for OrderId: {}. Cancelling order.", order.getId());
                    order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
                }
                orderRepository.save(order);
            });
        };
    }

//    @Bean
//    public Consumer<InventoryEvent> inventoryEventConsumer() {
//        return (inventory) -> {
//            log.info("Order Service received InventoryEvent: {}", inventory);
//            orderRepository.findById(inventory.getOrderRequestDto().getOrderId()).ifPresent(order -> {
//                if (InventoryStatus.INVENTORY_UPDATED.equals(inventory.getInventoryStatus())) {
//                    log.info("Inventory updated for OrderId: {}. Completing order.", order.getId());
//                    order.setOrderStatus(OrderStatus.ORDER_COMPLETED);
//                } else {
//                    log.error("Inventory check failed for OrderId: {}. Cancelling order.", order.getId());
//                    order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
//                }
//                orderRepository.save(order);
//            });
//        };
//    }
}
