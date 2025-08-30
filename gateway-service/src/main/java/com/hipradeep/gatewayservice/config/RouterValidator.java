package com.hipradeep.gatewayservice.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

	public static final List<String> openEndpoints = List.of(
			"/public",
			"/auth/login",
			"/api/orders"
	);

	public Predicate<ServerHttpRequest> isSecured = serverHttpRequest -> openEndpoints.stream()
			.noneMatch(uri -> serverHttpRequest.getURI().getPath().contains(uri));
}
