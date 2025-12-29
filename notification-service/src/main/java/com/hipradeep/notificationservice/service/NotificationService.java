package com.hipradeep.notificationservice.service;

import com.hipradeep.notificationservice.model.Notification;
import com.hipradeep.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(Notification notification) {
        log.info("Creating notification for recipient: {}", notification.getRecipient());
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        log.info("Fetching all notifications");
        return notificationRepository.findAll();
    }

    public Notification getNotificationById(Long id) {
        log.info("Fetching notification by id: {}", id);
        return notificationRepository.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    public void deleteNotification(Long id) {
        log.info("Deleting notification with id: {}", id);
        notificationRepository.deleteById(id);
    }
}
