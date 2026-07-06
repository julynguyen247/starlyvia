package org.example.authservice.event;

public interface DomainEventPublisher {
    void publish(String topic, String key, Object payload);
}
