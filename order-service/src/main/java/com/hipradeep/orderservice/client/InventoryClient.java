package com.hipradeep.orderservice.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryClient {

    @Autowired
    private RestTemplate restTemplate;

    @Retryable(value = { RuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean checkStock(String skuCode) {
        System.out.println("Calling Inventory Service for skuCode: " + skuCode);

        // This call might fail based on InventoryService simulation
        return restTemplate.getForObject("http://localhost:8084/api/inventory/" + skuCode, Boolean.class);
    }

    @Recover
    public boolean recover(RuntimeException e, String skuCode) {
        System.out.println(
                "Max retries reached. Recovering from failure for skuCode: " + skuCode + ". Error: " + e.getMessage());
        return false; // Fallback response
    }
}
