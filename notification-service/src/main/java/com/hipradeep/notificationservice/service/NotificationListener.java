package com.hipradeep.notificationservice.service;

import com.hipradeep.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationRepository notificationRepository;

    public void logNotification(String message) {
        log.info("Notification Service is active. Message: {}", message);
    }
}
