package com.hipradeep.gatewayservice.filter;

import com.hipradeep.gatewayservice.model.GatewayLog;
import com.hipradeep.gatewayservice.repository.GatewayLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String REQUEST_BODY_KEY = "cachedRequestBody";
    
    private final GatewayLogRepository gatewayLogRepository;

    public LoggingFilter(GatewayLogRepository gatewayLogRepository) {
        this.gatewayLogRepository = gatewayLogRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();
        LocalDateTime requestTime = LocalDateTime.now();
        
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String clientIp = request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";

        logger.info("Incoming request: {} {} | Client IP: {}", method, path, clientIp);

        MediaType contentType = request.getHeaders().getContentType();
        boolean isLoggable = contentType != null && (
                contentType.includes(MediaType.APPLICATION_JSON) ||
                contentType.includes(MediaType.APPLICATION_XML) ||
                contentType.includes(MediaType.APPLICATION_FORM_URLENCODED) ||
                contentType.includes(MediaType.TEXT_PLAIN) ||
                contentType.includes(MediaType.TEXT_XML) ||
                contentType.includes(MediaType.TEXT_HTML)
        );

        ServerWebExchange mutatedExchange = exchange;
        if (isLoggable) {
            ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(request) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return super.getBody().map(dataBuffer -> {
                        // Read buffer without changing the read pointer of the original buffer
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.slice(dataBuffer.readPosition(), dataBuffer.readableByteCount()).read(bytes);
                        
                        String body = new String(bytes, StandardCharsets.UTF_8);
                        String existingBody = exchange.getAttributeOrDefault(REQUEST_BODY_KEY, "");
                        exchange.getAttributes().put(REQUEST_BODY_KEY, existingBody + body);
                        
                        return dataBuffer;
                    });
                }
            };
            mutatedExchange = exchange.mutate().request(decoratedRequest).build();
        }

        final ServerWebExchange finalExchange = mutatedExchange;

        return chain.filter(finalExchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            HttpStatusCode status = finalExchange.getResponse().getStatusCode();
            int statusCode = status != null ? status.value() : 200;
            
            String payload = finalExchange.getAttributeOrDefault(REQUEST_BODY_KEY, "");
            if (!isLoggable && contentType != null) {
                payload = "[non-loggable binary/multipart payload]";
            }

            logger.info("Outgoing response: Status {} for {} {} | Processing Time: {}ms", 
                    statusCode, method, path, duration);

            // Asynchronously save to the database using the boundedElastic scheduler
            // to avoid blocking the main reactive event loop thread.
            final String finalPayload = payload;
            Mono.fromRunnable(() -> {
                GatewayLog log = new GatewayLog(null, method, path, clientIp, statusCode, duration, finalPayload, requestTime);
                gatewayLogRepository.save(log);
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
        }));
    }

    @Override
    public int getOrder() {
        // Run first to track overall request lifecycle duration
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
