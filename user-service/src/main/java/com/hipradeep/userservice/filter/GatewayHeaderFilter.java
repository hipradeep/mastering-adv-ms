package com.hipradeep.userservice.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GatewayHeaderFilter implements Filter {

    private static final String GATEWAY_SECRET_HEADER = "X-Gateway-Secret";
    private static final String GATEWAY_SECRET_VALUE = "my-secure-shared-gateway-secret-123";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        
        // Allow Actuator health checks and public endpoints to bypass this if hit directly
        if (path.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String secret = httpRequest.getHeader(GATEWAY_SECRET_HEADER);

        if (secret == null || !secret.equals(GATEWAY_SECRET_VALUE)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Direct access is not allowed\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
