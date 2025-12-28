package com.hipradeep.notificationservice.service;

import com.hipradeep.notificationservice.model.Notification;
import com.hipradeep.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void sendNotification(Long orderNumber) {
        log.info("Sending notification for Order Number: {}", orderNumber);
        Notification notification = Notification.builder()
                .orderNumber(orderNumber)
                .status("SENT")
                .build();
        notificationRepository.save(notification);
        log.info("Notification sent and saved for Order Number: {}", orderNumber);
    }
}
