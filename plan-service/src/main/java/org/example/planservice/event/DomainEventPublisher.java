package org.example.planservice.event;

public interface DomainEventPublisher {
    void publish(String topic, String key, Object payload);
}
