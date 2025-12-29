package com.hipradeep.inventoryservice.config;

import com.hipradeep.inventoryservice.service.InventoryService;
import com.hipradeep.saga.commons.event.InventoryEvent;
import com.hipradeep.saga.commons.event.PaymentEvent;
import com.hipradeep.saga.commons.event.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
@Slf4j
public class InventoryConfig {

    @Autowired
    private InventoryService inventoryService;

    @Bean
    public Function<Flux<PaymentEvent>, Flux<InventoryEvent>> inventoryProcessor() {
        return flux -> flux.doOnNext(event -> log.info("Inventory Processor received event: {}", event))
                .flatMap(this::processInventory);
    }

    private Mono<InventoryEvent> processInventory(PaymentEvent paymentEvent) {
        if (PaymentStatus.PAYMENT_COMPLETED.equals(paymentEvent.getPaymentStatus())) {
            return Mono.fromSupplier(() -> inventoryService.newPaymentEvent(paymentEvent));
        }
        return Mono.empty();
    }
}
