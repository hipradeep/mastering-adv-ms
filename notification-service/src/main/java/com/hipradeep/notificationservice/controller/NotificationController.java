package com.hipradeep.notificationservice.controller;

import com.hipradeep.notificationservice.service.NotificationListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationListener notificationListener;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String sendNotification(@RequestBody String message) {
        log.info("Received request to send manual notification: {}", message);
        notificationListener.logNotification(message);
        return "Notification logged successfully";
    }
}
