package com.hipradeep.notificationservice.controller;

import com.hipradeep.notificationservice.service.KafkaProducerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final KafkaProducerService kafkaProducerService;

    public NotificationController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping("/send")
    public String sendNotification(@RequestBody String notificationData) {
        String message = "NOTIFICATION_SENT: " + notificationData;
        kafkaProducerService.sendMessage(message);
        return "Notification sent event published: " + message;
    }
}
