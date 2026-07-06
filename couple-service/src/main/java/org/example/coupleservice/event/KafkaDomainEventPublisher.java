package org.example.coupleservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaDomainEventPublisher implements DomainEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
        } catch (Exception ex) {
            log.warn("Failed to publish domain event to topic {}", topic, ex);
        }
    }
}
