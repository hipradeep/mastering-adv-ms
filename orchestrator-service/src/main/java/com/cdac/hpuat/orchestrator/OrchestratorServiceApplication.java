package com.cdac.hpuat.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class OrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorServiceApplication.class, args);
    }

}
