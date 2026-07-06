package org.example.coupleservice;

import org.example.coupleservice.client.UserClient;
import org.example.coupleservice.entity.Couple;
import org.example.coupleservice.entity.CoupleRequest;
import org.example.coupleservice.entity.CoupleRequestStatus;
import org.example.coupleservice.event.DomainEventPublisher;
import org.example.coupleservice.repository.CoupleRepository;
import org.example.coupleservice.repository.CoupleRequestRepository;
import org.example.coupleservice.service.CoupleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CoupleServiceApplicationTests {

    @Autowired
    private CoupleService coupleService;

    @Autowired
    private CoupleRequestRepository coupleRequestRepository;

    @Autowired
    private CoupleRepository coupleRepository;

    @TestConfiguration
    static class UserClientTestConfig {
        @Bean
        @Primary
        UserClient userClient() {
            return userId -> true;
        }

        @Bean
        @Primary
        DomainEventPublisher domainEventPublisher() {
            return (topic, key, payload) -> {
            };
        }
    }

    @BeforeEach
    void cleanDatabase() {
        coupleRepository.deleteAll();
        coupleRequestRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void sendAndAcceptCoupleRequestStoresOnlyUserIds() {
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        CoupleRequest request = coupleService.sendRequest(requesterId, receiverId);
        Couple couple = coupleService.acceptRequest(receiverId, request.getId());
        CoupleRequest acceptedRequest = coupleRequestRepository.findById(request.getId()).orElseThrow();

        assertThat(request.getRequesterId()).isEqualTo(requesterId);
        assertThat(request.getReceiverId()).isEqualTo(receiverId);
        assertThat(acceptedRequest.getStatus()).isEqualTo(CoupleRequestStatus.ACCEPTED);
        assertThat(coupleRepository.existsBetween(requesterId, receiverId)).isTrue();
        assertThat(couple.getUserId()).isIn(requesterId, receiverId);
        assertThat(couple.getPartnerId()).isIn(requesterId, receiverId);
    }

    @Test
    void rejectedRequestCanBeSentAgainWithoutCreatingUserTable() {
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        CoupleRequest request = coupleService.sendRequest(requesterId, receiverId);
        coupleService.rejectRequest(receiverId, request.getId());

        CoupleRequest resentRequest = coupleService.sendRequest(requesterId, receiverId);

        assertThat(resentRequest.getId()).isEqualTo(request.getId());
        assertThat(resentRequest.getStatus()).isEqualTo(CoupleRequestStatus.PENDING);
        assertThat(coupleRequestRepository.findAll()).hasSize(1);
    }

    @Test
    void userCanOnlyHaveOneActiveCouple() {
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        CoupleRequest request = coupleService.sendRequest(requesterId, receiverId);
        coupleService.acceptRequest(receiverId, request.getId());

        assertThatThrownBy(() -> coupleService.sendRequest(otherUserId, receiverId))
                .hasMessageContaining("already in a couple");
    }

    @Test
    void cannotSendRequestToMissingReceiver() {
        UserClient missingUserClient = userId -> false;
        DomainEventPublisher eventPublisher = (topic, key, payload) -> {
        };
        CoupleService service = new CoupleService(
                coupleRequestRepository,
                coupleRepository,
                missingUserClient,
                eventPublisher
        );

        assertThatThrownBy(() -> service.sendRequest(UUID.randomUUID(), UUID.randomUUID()))
                .hasMessageContaining("Receiver user not found");
    }
}
