package org.example.coupleservice.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false")
public class NoopDomainEventPublisher implements DomainEventPublisher {
    @Override
    public void publish(String topic, String key, Object payload) {
    }
}
