package com.sparktech.happyendings.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparktech.happyendings.event.SystemEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> void sendEvent(String topic, T payload) {
        SystemEvent<T> systemEvent = new SystemEvent<>(topic, payload);
        try {
            String jsonMessage = objectMapper.writeValueAsString(systemEvent);
            if (kafkaTemplate != null) {
                kafkaTemplate.send(topic, systemEvent.getEventId(), jsonMessage).whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sent event successfully to topic: {} [ID: {}]", topic, systemEvent.getEventId());
                    } else {
                        log.error("Failed to send event to topic: {} [ID: {}]", topic, systemEvent.getEventId(), ex);
                    }
                });
            } else {
                log.info("[In-Memory fallback] Emitted local event to topic '{}': {}", topic, jsonMessage);
            }
        } catch (Exception e) {
            log.error("Failed to serialize and emit event on topic: {}", topic, e);
        }
    }
}
