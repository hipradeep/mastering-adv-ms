package com.hipradeep.orderservice.service;

import com.hipradeep.orderservice.model.Order;
import com.hipradeep.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final io.micrometer.tracing.Tracer tracer;

    public String placeOrder(Order orderRequest) {
        io.micrometer.tracing.Span newSpan = tracer.nextSpan().name("order-processing");

        try (io.micrometer.tracing.Tracer.SpanInScope ws = tracer.withSpan(newSpan.start())) {
            log.info("Placing Order for Product Code: {}", orderRequest.getSkuCode());

            // Call Inventory Service
            log.info("Calling Inventory Service to reduce stock");
            Boolean stockReduced = restTemplate.exchange(
                    "http://localhost:8083/api/inventory/reduce/" + orderRequest.getSkuCode() + "?quantity="
                            + orderRequest.getQuantity(),
                    org.springframework.http.HttpMethod.PUT,
                    null,
                    Boolean.class).getBody();

            if (Boolean.TRUE.equals(stockReduced)) {
                orderRequest.setOrderNumber(UUID.randomUUID().toString());
                orderRepository.save(orderRequest);
                log.info("Order Placed Successfully with Order Number: {}", orderRequest.getOrderNumber());

                // Call Notification Service
                log.info("Calling Notification Service to send notification");
                restTemplate.postForObject("http://localhost:8084/api/notification", orderRequest.getId(),
                        String.class);

                return "Order Placed Successfully";
            } else {
                log.warn("Product is not in stock or insufficient quantity, please try again later");
                throw new IllegalArgumentException(
                        "Product is not in stock or insufficient quantity, please try again later");
            }
        } finally {
            newSpan.end();
        }
    }
}
