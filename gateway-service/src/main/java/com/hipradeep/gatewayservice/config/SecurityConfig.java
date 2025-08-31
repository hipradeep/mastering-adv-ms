package com.hipradeep.gatewayservice.config;

import com.hipradeep.gatewayservice.filter.JwtReactiveAuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtReactiveAuthenticationManager authManager;

    /**
     * Configures a security context repository for storing authentication information.
     * Using WebSessionServerSecurityContextRepository stores the security context
     * in the web session, which is useful for stateful applications.
     *
     * Note: For pure JWT/stateless authentication, this might not be necessary as
     * we're using NoOpServerSecurityContextRepository in the filter.
     */


    /**
     * Main security configuration that defines the filter chain and authorization rules.
     * This method:
     * 1. Disables CSRF (appropriate for API gateways with JWT)
     * 2. Configures JWT authentication filter
     * 3. Defines authorization rules for different endpoints
     * 4. Sets up the security filter chain
     *
     * @param http ServerHttpSecurity builder for configuring web security
     * @return Configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Create JWT authentication filter with custom authentication manager
        AuthenticationWebFilter jwtAuthFilter = new AuthenticationWebFilter(authManager);

        // Configure the JWT filter with:
        // 1. Authentication converter - extracts JWT token from request
        // 2. Security context repository - uses NoOp (stateless) for JWT
        // 3. Success handler - processes successful authentication and sets headers
        jwtAuthFilter.setServerAuthenticationConverter(SecurityConfig::convertJwtToAuthentication);
        jwtAuthFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        jwtAuthFilter.setAuthenticationSuccessHandler(SecurityConfig::handleAuthenticationSuccess);

        return http
                // Disable CSRF protection as it's not needed for stateless JWT APIs
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Configure authorization rules
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - no authentication required
                        .pathMatchers("/public/**", "/auth/login", "/api/orders").permitAll()

                        // Admin-only endpoints - require ADMIN role
                        .pathMatchers("/api/users/wallets/**").hasRole("ADMIN")

                        // User endpoints - require either USER or ADMIN role
                        .pathMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")

                        // All other endpoints require authentication (but no specific role)
                        .anyExchange().authenticated())

                // Add JWT authentication filter at the AUTHENTICATION position in the filter chain
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                // Build the security filter chain
                .build();
    }

    /**
     * Converts incoming HTTP request to Authentication object by extracting JWT token.
     * This method is called by Spring Security to convert the raw HTTP request into
     * an authentication object that can be validated by the AuthenticationManager.
     *
     * Flow: HTTP Request → Extract Authorization header → Create Authentication object
     *
     * @param exchange The ServerWebExchange containing request/response information
     * @return Mono containing Authentication object if token found, empty otherwise
     */
    private static Mono<Authentication> convertJwtToAuthentication(ServerWebExchange exchange) {
        // Extract Authorization header from the request
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Check if header exists and follows Bearer token format
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract the actual token (remove "Bearer " prefix)
            String token = authHeader.substring(7);
            log.debug("🔐 Extracted JWT token from Authorization header");

            // Create an unauthenticated Authentication object containing the token
            // The AuthenticationManager will validate this token and create an authenticated version
            return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
        }

        // No valid Bearer token found - return empty to indicate unauthenticated request
        log.info("❌ No Bearer token found in Authorization header");
        return Mono.empty();
    }

    /**
     * Handles successful authentication by setting custom headers on the request.
     * This method is called after successful JWT validation and:
     * 1. Extracts user information from the authenticated token
     * 2. Adds X-Username and X-Roles headers to the request
     * 3. Continues the filter chain with the modified request
     *
     * Flow: Successful Authentication → Extract user info → Add headers → Continue
     *
     * @param webFilterExchange Contains the current exchange and filter chain
     * @param authentication The successfully authenticated Authentication object
     * @return Mono<Void> indicating completion of the success handling
     */
    private static Mono<Void> handleAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        log.info("✅ Authentication successful for user: {}", authentication.getName());

        // Extract current exchange from the filter context
        ServerWebExchange exchange = webFilterExchange.getExchange();

        // Extract username from authenticated principal
        String username = authentication.getName();

        // Extract roles and convert to comma-separated string (without ROLE_ prefix)
        String roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.joining(","));

        log.info("👤 Setting headers for user: {} with roles: {}", username, roles);

        // Create a mutated request with additional headers
        // These headers will be available to downstream services
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-Username", username)      // Forward username to downstream services
                .header("X-Roles", roles)           // Forward roles to downstream services
                .header("X-Source", "gateway")  // Forward source to downstream services
                .build();

        // Continue the filter chain with the modified request
        return webFilterExchange.getChain().filter(exchange.mutate().request(mutatedRequest).build());
    }
}