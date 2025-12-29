package com.hipradeep.orderservice.service;

import com.hipradeep.saga.commons.event.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

@Service
public class OrderStatusPublisher {

    @Autowired
    private Sinks.Many<OrderEvent> orderSinks;

    public void publishOrderEvent(OrderEvent orderEvent) {
        orderSinks.tryEmitNext(orderEvent);
    }
}
