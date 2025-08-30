package com.hipradeep.gatewayservice.filter;

import com.hipradeep.gatewayservice.config.RouterValidator;
import com.hipradeep.gatewayservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
@RefreshScope
public class AuthenticationFilter implements GatewayFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private RouterValidator routerValidator;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Log the incoming request URL
        log.info("Incoming request URL: {}", exchange.getRequest().getURI());
        log.info("HTTP Method: {}", exchange.getRequest().getMethod());
        log.info("Authorization header: {}", exchange.getRequest().getHeaders().getFirst("Authorization"));

        ServerHttpRequest request = exchange.getRequest();

        // Skip JWT validation for public endpoints using RouterValidator
        if (!routerValidator.isSecured.test(request)) {
            log.info("Skipped JWT Validation for: {}", request.getURI().getPath());
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for: {}", request.getURI().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract and validate JWT token
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid JWT token for: {}", request.getURI().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        List<String> roles = jwtUtil.getRoles(token);
        log.warn("Access Roles: {}", roles);

        // Restrict /api/users/wallets/** to ADMIN
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/api/users/wallets") && !roles.contains("ADMIN")) {
            log.warn("Invalid Access, Wallets are only for ADMIN: {}", request.getURI().getPath());
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // Extract username from token and add it to headers for downstream services
        String username = jwtUtil.getUsername(token);
        if (username != null) {
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Username", username)
                    .header("X-Roles", String.join(",", roles))
                    .build();

            exchange = exchange.mutate().request(mutatedRequest).build();
            log.info("Added X-Username header for downstream services: {}", username);
        }

        log.info("JWT validation successful for: {}", request.getURI().getPath());
        return chain.filter(exchange);
    }
}
