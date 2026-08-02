package com.example.s3trigger.service;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ProcessorService {

    private final S3Client s3Client;

    @Value("${aws.s3.processed-bucket:sample-processed-bucket}")
    private String processedBucketName;

    public void processS3Event(S3EventNotification s3EventNotification) {
        log.info("=== Spring Cloud Function S3 Event Processing Started ===");

        if (s3EventNotification == null || s3EventNotification.getRecords() == null) {
            log.warn("Received empty or null S3 event notification records.");
            return;
        }

        for (S3EventNotification.S3EventNotificationRecord record : s3EventNotification.getRecords()) {
            String eventName = record.getEventName();
            String bucketName = record.getS3().getBucket().getName();
            String objectKey = URLDecoder.decode(record.getS3().getObject().getKey(), StandardCharsets.UTF_8);
            Long objectSize = record.getS3().getObject().getSizeAsLong();

            log.info("Event Name: {}", eventName);
            log.info("Source Bucket: {}", bucketName);
            log.info("Object Key: {}", objectKey);
            log.info("Object Size: {} bytes", objectSize);

            try {
                // 1. Retrieve Object Metadata from S3
                HeadObjectResponse headObjectResponse = null;
                try {
                    headObjectResponse = s3Client.headObject(HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build());
                } catch (Exception ex) {
                    log.warn("Unable to fetch headObject metadata for s3://{}/{}: {}", bucketName, objectKey, ex.getMessage());
                }

                String contentType = (headObjectResponse != null && headObjectResponse.contentType() != null) 
                        ? headObjectResponse.contentType() 
                        : "application/octet-stream";

                log.info("Fetched Object Metadata: Content-Type={}", contentType);

                // 2. Perform Automated Business Processing Logic
                String processedSummaryJson = String.format("""
                        {
                          "sourceBucket": "%s",
                          "sourceKey": "%s",
                          "sizeBytes": %d,
                          "contentType": "%s",
                          "processedAt": "%s",
                          "status": "PROCESSED_SUCCESSFULLY"
                        }
                        """, bucketName, objectKey, objectSize, contentType, Instant.now().toString());

                // 3. Write summary result to Processed Destination Bucket
                if (processedBucketName != null && !processedBucketName.isBlank()) {
                    String outputKey = "processed-records/" + System.currentTimeMillis() + "-" + getFileName(objectKey) + ".json";

                    s3Client.putObject(PutObjectRequest.builder()
                                    .bucket(processedBucketName)
                                    .key(outputKey)
                                    .contentType("application/json")
                                    .build(),
                            RequestBody.fromString(processedSummaryJson, StandardCharsets.UTF_8));

                    log.info("Saved processing output record to S3 destination bucket: {}/{}", processedBucketName, outputKey);
                }

            } catch (Exception e) {
                log.error("Failed to process S3 object [{}] from bucket [{}]: {}", objectKey, bucketName, e.getMessage(), e);
                throw new RuntimeException("S3 Event processing failure", e);
            }
        }

        log.info("=== Spring Cloud Function S3 Event Processing Completed ===");
    }

    private String getFileName(String key) {
        if (key == null) return "unknown";
        int lastSlash = key.lastIndexOf('/');
        return (lastSlash >= 0) ? key.substring(lastSlash + 1) : key;
    }
}
