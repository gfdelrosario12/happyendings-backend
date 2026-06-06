package com.sparktech.happyendings.event;

import java.util.UUID;

public class SystemEvent<T> {
    private String eventId;
    private String topic;
    private T payload;
    private long timestamp;

    public SystemEvent() {}

    public SystemEvent(String topic, T payload) {
        this.eventId = UUID.randomUUID().toString();
        this.topic = topic;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
