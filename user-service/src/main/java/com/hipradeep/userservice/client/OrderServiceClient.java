package com.hipradeep.userservice.client;

import com.hipradeep.userservice.dto.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${order.service.base-url:http://order-service}")
    private String orderServiceBaseUrl;

    public List<Order> getOrdersByCustomerId(Long customerId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(orderServiceBaseUrl + "/api/orders/customer/{customerId}", customerId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Order>>() {})
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.info("No orders found for customer ID: {}", customerId);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching orders for customer ID: {}", customerId, e);
            return Collections.emptyList();
        }
    }

    public Order getOrderById(Long orderId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(orderServiceBaseUrl + "/api/orders/{id}", orderId)
                    .retrieve()
                    .bodyToMono(Order.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.info("Order not found with ID: {}", orderId);
            return null;
        } catch (Exception e) {
            log.error("Error fetching order by ID: {}", orderId, e);
            return null;
        }
    }

    public List<Order> getAllOrders() {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(orderServiceBaseUrl + "/api/orders")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Order>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Error fetching all orders", e);
            return Collections.emptyList();
        }
    }

    public Order createOrder(Order order) {
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(orderServiceBaseUrl + "/api/orders")
                    .bodyValue(order)
                    .retrieve()
                    .bodyToMono(Order.class)
                    .block();
        } catch (Exception e) {
            log.error("Error creating order", e);
            return null;
        }
    }

    public boolean deleteOrder(Long orderId) {
        try {
            webClientBuilder.build()
                    .delete()
                    .uri(orderServiceBaseUrl + "/api/orders/{id}", orderId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            log.info("Order not found for deletion with ID: {}", orderId);
            return false;
        } catch (Exception e) {
            log.error("Error deleting order ID: {}", orderId, e);
            return false;
        }
    }

    // Additional method with better error handling using onStatus
    public List<Order> getOrdersByCustomerIdWithBetterErrorHandling(Long customerId) {
        return webClientBuilder.build()
                .get()
                .uri(orderServiceBaseUrl + "/api/orders/customer/{customerId}", customerId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    log.warn("Client error when fetching orders for customer ID: {}", customerId);
                    return Mono.error(new RuntimeException("Client error: " + response.statusCode()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    log.error("Server error when fetching orders for customer ID: {}", customerId);
                    return Mono.error(new RuntimeException("Server error: " + response.statusCode()));
                })
                .bodyToMono(new ParameterizedTypeReference<List<Order>>() {})
                .onErrorResume(e -> {
                    log.error("Error in WebClient call for customer ID: {}", customerId, e);
                    return Mono.just(Collections.emptyList());
                })
                .block();
    }
}