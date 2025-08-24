package com.hipradeep.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:application.yml")
@SpringBootApplication
public class ApiGatewayServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayServiceApplication.class, args);

    }
}
