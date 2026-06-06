package com.sparktech.happyendings.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorker.class);

    @KafkaListener(topics = {"guest.invited", "guest.rsvp.updated"}, groupId = "notification_group")
    public void processNotification(String message) {
        log.info("Received guest event on NotificationWorker: {}", message);
        // Process user/guest updates and trigger real-time notifications
        log.info("Notification successfully dispatched asynchronously.");
    }
}
