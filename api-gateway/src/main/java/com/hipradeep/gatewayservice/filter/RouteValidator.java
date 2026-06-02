package com.hipradeep.gatewayservice.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/auth/login",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> {
                String path = request.getURI().getPath();
                String method = request.getMethod().name();
                // Allow user registration without a JWT token
                if ("/api/users".equals(path) && "POST".equalsIgnoreCase(method)) {
                    return false;
                }
                return openApiEndpoints
                        .stream()
                        .noneMatch(uri -> path.contains(uri));
            };
}
