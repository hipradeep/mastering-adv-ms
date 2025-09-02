package com.hipradeep.userservice.client;


import com.hipradeep.userservice.dto.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final RestTemplate restTemplate; // Injected LoadBalanced RestTemplate, to work with eureka

    //@Value("${order.service.base-url:http://localhost:8083}")
    @Value("${order.service.base-url:http://order-service}")
    private String orderServiceBaseUrl;

    //private String orderServiceBaseUrl ="http://order-service";

    public List<Order> getOrdersByCustomerId(Long customerId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(orderServiceBaseUrl)
                    .path("/api/orders/customer/{customerId}")
                    .buildAndExpand(customerId)
                    .toUriString();

            ResponseEntity<List<Order>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Order>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching orders for customer ID: {}", customerId, e);
            return Collections.emptyList();
        }
    }

    public Order getOrderById(Long orderId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(orderServiceBaseUrl)
                    .path("/api/orders/{id}")
                    .buildAndExpand(orderId)
                    .toUriString();

            return restTemplate.getForObject(url, Order.class);
        } catch (Exception e) {
            log.error("Error fetching order by ID: {}", orderId, e);
            return null;
        }
    }

    public List<Order> getAllOrders() {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(orderServiceBaseUrl)
                    .path("/api/orders")
                    .toUriString();

            ResponseEntity<List<Order>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Order>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching all orders", e);
            return Collections.emptyList();
        }
    }

    public Order createOrder(Order order) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(orderServiceBaseUrl)
                    .path("/api/orders")
                    .toUriString();

            return restTemplate.postForObject(url, order, Order.class);
        } catch (Exception e) {
            log.error("Error creating order", e);
            return null;
        }
    }

    public boolean deleteOrder(Long orderId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(orderServiceBaseUrl)
                    .path("/api/orders/{id}")
                    .buildAndExpand(orderId)
                    .toUriString();

            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            log.error("Error deleting order ID: {}", orderId, e);
            return false;
        }
    }
}
