package com.hipradeep.notificationservice.controller;

import com.hipradeep.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String sendNotification(@RequestBody Long orderNumber) {
        log.info("Received notification request for orderNumber: {}", orderNumber);
        notificationService.sendNotification(orderNumber);
        return "Notification Sent Successfully";
    }
}
