package org.example.dateplanservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dateplanservice.entity.CoupleMembership;
import org.example.dateplanservice.repository.CoupleMembershipRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class CoupleEventConsumer {
    private final CoupleMembershipRepository coupleMembershipRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.couple-accepted:couple.accepted}")
    public void onCoupleAccepted(String message) {
        try {
            CoupleAcceptedEvent event = objectMapper.readValue(message, CoupleAcceptedEvent.class);

            coupleMembershipRepository.save(CoupleMembership.builder()
                    .coupleId(event.coupleId())
                    .userId(event.userId())
                    .partnerId(event.partnerId())
                    .updatedAt(LocalDateTime.now())
                    .build());
        } catch (RuntimeException ex) {
            log.warn("Failed to consume couple.accepted event", ex);
        } catch (Exception ex) {
            log.warn("Failed to parse couple.accepted event", ex);
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.couple-removed:couple.removed}")
    public void onCoupleRemoved(String message) {
        try {
            CoupleRemovedEvent event = objectMapper.readValue(message, CoupleRemovedEvent.class);
            coupleMembershipRepository.deleteById(event.coupleId());
        } catch (RuntimeException ex) {
            log.warn("Failed to consume couple.removed event", ex);
        } catch (Exception ex) {
            log.warn("Failed to parse couple.removed event", ex);
        }
    }
}
