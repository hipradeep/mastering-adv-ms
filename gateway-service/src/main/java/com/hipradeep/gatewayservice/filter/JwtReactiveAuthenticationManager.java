package com.hipradeep.gatewayservice.filter;

import com.hipradeep.gatewayservice.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.stream.Collectors;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private static final Logger log = LoggerFactory.getLogger(JwtReactiveAuthenticationManager.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        log.debug("Starting JWT authentication process");

        String token = authentication.getCredentials().toString();
        log.trace("Extracted JWT token: {}", maskToken(token));

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            log.warn("JWT token validation failed for token: {}", maskToken(token));
            return Mono.error(new BadCredentialsException("Invalid JWT token"));
        }
        log.debug("JWT token validation successful");

        // Extract user information
        String username = jwtUtil.getUsername(token);
        log.debug("Extracted username from token: {}", username);

        var authorities = jwtUtil.getRoles(token).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        log.info("Successfully authenticated user: {} with roles: {} for token: {}",
                username, authorities, maskToken(token));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, token, authorities);

        return Mono.just(auth);
    }

    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}

