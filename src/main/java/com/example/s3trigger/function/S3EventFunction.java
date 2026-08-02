package com.example.s3trigger.function;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.example.s3trigger.service.S3ProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class S3EventFunction {

    private final S3ProcessorService s3ProcessorService;

    /**
     * Spring Cloud Function bean that accepts S3EventNotification from AWS Lambda
     * and returns a processing confirmation response string.
     */
    @Bean
    public Function<S3EventNotification, String> s3EventConsumer() {
        return event -> {
            log.info("Received S3EventNotification in Spring Cloud Function...");
            s3ProcessorService.processS3Event(event);
            return "SUCCESS: Processed " + (event.getRecords() != null ? event.getRecords().size() : 0) + " record(s)";
        };
    }
}
