package com.example.s3trigger;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.example.s3trigger.service.S3ProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.Collections;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
class S3EventFunctionTest {

    @MockBean
    private S3Client s3Client;

    @Autowired
    private Function<S3EventNotification, String> s3EventConsumer;

    @Test
    void contextLoads() {
        assertNotNull(s3EventConsumer);
    }

    @Test
    void testS3EventConsumerWithMockEvent() {
        S3EventNotification.S3BucketEntity bucketEntity = new S3EventNotification.S3BucketEntity(
                "test-upload-bucket",
                new S3EventNotification.UserIdentityEntity("test-owner"),
                "arn:aws:s3:::test-upload-bucket"
        );

        S3EventNotification.S3ObjectEntity objectEntity = new S3EventNotification.S3ObjectEntity(
                "incoming/data.json",
                1024L,
                "eTag123",
                "seq123",
                "version1"
        );

        S3EventNotification.S3Entity s3Entity = new S3EventNotification.S3Entity(
                "configurationId",
                bucketEntity,
                objectEntity,
                "1.0"
        );

        S3EventNotification.S3EventNotificationRecord record = new S3EventNotification.S3EventNotificationRecord(
                "us-east-1",
                "ObjectCreated:Put",
                "aws:s3",
                "2026-08-02T07:45:00.000Z",
                "2.1",
                new S3EventNotification.RequestParametersEntity("127.0.0.1"),
                new S3EventNotification.ResponseElementsEntity("req123", "id2"),
                s3Entity,
                new S3EventNotification.UserIdentityEntity("principal123")
        );

        S3EventNotification s3EventNotification = new S3EventNotification(Collections.singletonList(record));

        Mockito.when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().contentType("application/json").eTag("mock-etag").build());

        String result = s3EventConsumer.apply(s3EventNotification);

        assertNotNull(result);
        assertTrue(result.contains("SUCCESS"));
    }
}
