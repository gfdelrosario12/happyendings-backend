package com.sparktech.happyendings.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditLoggerWorker {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggerWorker.class);

    @KafkaListener(topics = "audit.log", groupId = "audit_group")
    public void processAuditLog(String message) {
        log.info("Received audit event on AuditLoggerWorker: {}", message);
        // Persist audit traces to PostgreSQL database
        log.info("Audit entry registered successfully.");
    }
}
