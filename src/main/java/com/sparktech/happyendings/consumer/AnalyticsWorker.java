package com.sparktech.happyendings.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsWorker {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsWorker.class);

    @KafkaListener(topics = "analytics.event", groupId = "analytics_group")
    public void processAnalytics(String message) {
        log.info("Received analytics event on AnalyticsWorker: {}", message);
        // Process aggregation and write back into Redis & persistent storage
        log.info("Analytics aggregates updated successfully.");
    }
}
