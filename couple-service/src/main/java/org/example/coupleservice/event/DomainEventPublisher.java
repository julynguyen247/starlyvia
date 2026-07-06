package org.example.coupleservice.event;

public interface DomainEventPublisher {
    void publish(String topic, String key, Object payload);
}
