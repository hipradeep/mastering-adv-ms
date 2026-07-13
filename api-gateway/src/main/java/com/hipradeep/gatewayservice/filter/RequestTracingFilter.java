package com.hipradeep.gatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestTracingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // Generate a unique ID for this request
        String traceId = UUID.randomUUID().toString();

        // Attach to request so downstream services can log it
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-Trace-Id", traceId)
                .build();

        // Also attach to response so client can correlate
        exchange.getResponse().getHeaders().add("X-Trace-Id", traceId);

        return chain.filter(exchange.mutate().request(mutated).build())
                .then(Mono.fromRunnable(() ->
                        System.out.println("After Response")));
    }

    @Override
    public int getOrder() { return -10; } // run very early
}
