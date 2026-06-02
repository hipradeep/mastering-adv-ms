package com.hipradeep.gatewayservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gateway_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;
    private String path;
    
    @Column(name = "client_ip")
    private String clientIp;
    
    @Column(name = "status_code")
    private Integer statusCode;
    
    @Column(name = "duration_ms")
    private Long durationMs;
    
    @Column(columnDefinition = "TEXT")
    private String payload;
    
    private LocalDateTime timestamp;
}
