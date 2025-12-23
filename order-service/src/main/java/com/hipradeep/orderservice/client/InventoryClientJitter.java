package com.hipradeep.orderservice.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryClientJitter {

    @Autowired
    private RestTemplate restTemplate;

    // Jitter: Random delay between 1000ms and 3000ms
    @Retryable(retryFor = {
            RuntimeException.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000, maxDelay = 3000, random = true))
    public boolean checkStockWithJitter(String skuCode) {
        System.out.println("Calling Inventory Service (Jitter Config) for skuCode: " + skuCode);

        // This call might fail based on InventoryService simulation
        return Boolean.TRUE
                .equals(restTemplate.getForObject("http://localhost:8084/api/inventory/" + skuCode, Boolean.class));
    }

    @Recover
    public boolean recover(RuntimeException e, String skuCode) {
        System.out.println(
                "Jitter (Spring Retry) Max retries reached. Recovering for skuCode: " + skuCode + ". Error: "
                        + e.getMessage());
        return false; // Fallback response
    }
}
