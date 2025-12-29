package com.hipradeep.notificationservice.config;

import com.hipradeep.saga.commons.event.InventoryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class NotificationConfig {

    @Bean
    public Consumer<InventoryEvent> notificationEventConsumer() {
        return inventoryEvent -> {
            log.info("--------------------------------------------------------------------------------");
            log.info("Order Processed for Order Id : {}", inventoryEvent.getOrderRequestDto().getOrderId());
            log.info("Inventory Status : {}", inventoryEvent.getInventoryStatus());
            log.info("--------------------------------------------------------------------------------");
        };
    }
}
