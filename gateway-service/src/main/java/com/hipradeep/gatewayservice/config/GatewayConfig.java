package com.hipradeep.gatewayservice.config;

import com.hipradeep.gatewayservice.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service - Public endpoint (no authentication filter)
                .route("auth-service",
                        r -> r.path("/auth/login/**")
                                .uri("http://localhost:8081"))

                // User Service - Secured endpoint (with authentication filter)
                .route("user-service",
                        r -> r.path("/api/users/**")
                                .filters(f -> f.filter(authenticationFilter))
                                .uri("http://localhost:8082"))

                // Order Service - Secured endpoint (with authentication filter)
                .route("order-service",
                        r -> r.path("/api/orders/**")
                                .filters(f -> f.filter(authenticationFilter))
                                .uri("http://localhost:8083"))

                // Inventory Service - Secured endpoint (with authentication filter)
//                .route("inventory-service",
//                        r -> r.path("/api/inventory/**")
//                                .filters(f -> f.filter(authenticationFilter).stripPrefix(2))
//                                .uri("lb://inventory-service")) // Load balanced
                .build();
    }
}