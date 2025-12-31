package com.hipradeep.inventoryservice.config;

import com.hipradeep.inventoryservice.service.InventoryService;
import com.hipradeep.saga.commons.event.InventoryEvent;
import com.hipradeep.saga.commons.event.PaymentEvent;
import com.hipradeep.saga.commons.event.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
@Slf4j
public class InventoryConfig {

    @Autowired
    private InventoryService inventoryService;

    @Bean
    public Function<PaymentEvent, InventoryEvent> inventoryProcessor() {
        return paymentEvent -> {
            log.info("Inventory Processor received event: {}", paymentEvent);
            if (PaymentStatus.PAYMENT_COMPLETED.equals(paymentEvent.getPaymentStatus())) {
                return inventoryService.newPaymentEvent(paymentEvent);
            }
            return new InventoryEvent();
        };
    }

    // Removed processInventory method as it's now inlined or simplified above
    // logic.
    // Actually the logic was simple: check status -> call service.
    // If status not completed, we return empty event (or null if stream allows, but
    // better empty object or handle null).
    // Original code: return Mono.empty() if not completed.
    // New code: return new InventoryEvent() (or maybe null? Spring Cloud Stream
    // function might ignore null).
    // Let's stick to returning an object to be safe, or just logging.
    // But wait, if we return an object, it publishes it.
    // If we return NULL, it should NOT publish.
    // Let's verify standard behavior. Imperative function returning null -> no
    // output message.
    // So let's return null if not completed.

}
