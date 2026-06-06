package com.sparktech.happyendings.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailWorker {

    private static final Logger log = LoggerFactory.getLogger(EmailWorker.class);

    @KafkaListener(topics = "notification.email.send", groupId = "email_group")
    public void processEmailSend(String message) {
        log.info("Received email event from Kafka: {}", message);
        // Simulate outbound SMTP email delivery
        log.info("Outbound email successfully delivered asynchronously.");
    }
}
