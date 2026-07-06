package org.example.groupservice.event;

public interface DomainEventPublisher {
    void publish(String topic, String key, Object payload);
}
