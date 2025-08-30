package com.hipradeep.gatewayservice.filter;

import com.hipradeep.gatewayservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // Log the incoming request URL
        log.info("Incoming request URL: {}", exchange.getRequest().getURI());
        log.info("HTTP Method: {}", exchange.getRequest().getMethod());
        log.info("Authorization header: {}", exchange.getRequest().getHeaders().getFirst("Authorization"));

        String path = exchange.getRequest().getURI().getPath();
        log.info("URI Path : {}", path);
        // Skip JWT validation for public endpoints
        if (path.startsWith("/public") || path.startsWith("/auth/login") || path.startsWith("/api/orders")) {
            log.info("Skipped JWT Validation: {}", exchange.getRequest().getMethod());
            return chain.filter(exchange);
        }

        // Continue with JWT validation...
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!JwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Optionally, you can store username in headers for downstream services
//        exchange.getRequest().mutate()
//                .header("X-Username", JwtUtil.getUsername(token))
//                .build();

        return chain.filter(exchange);
    }
}

