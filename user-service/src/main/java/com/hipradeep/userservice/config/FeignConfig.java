package com.hipradeep.userservice.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;  // For detailed logging
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(5000, 10000);  // Connect and read timeouts
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 2000, 3);  // Retry configuration
    }
}
