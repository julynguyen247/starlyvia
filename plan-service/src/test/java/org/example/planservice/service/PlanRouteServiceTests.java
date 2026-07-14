package org.example.planservice.service;

import org.example.planservice.client.RoutingClient;
import org.example.planservice.client.TravelMode;
import org.example.planservice.dto.ComputeRouteResponse;
import org.example.planservice.entity.Plan;
import org.example.planservice.entity.PlanStop;
import org.example.planservice.repository.PlanRepository;
import org.example.planservice.repository.PlanStopRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanRouteServiceTests {
    @Mock
    private PlanRepository planRepository;
    @Mock
    private PlanStopRepository planStopRepository;
    @Mock
    private PlanAccessPolicy planAccessPolicy;
    @Mock
    private RoutingClient routingClient;
    @InjectMocks
    private PlanRouteService planRouteService;

    @Test
    void sortsStopsBeforeCallingRoutingService() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        Plan plan = Plan.builder().id(planId).createdBy(userId).build();
        PlanStop second = stop(UUID.randomUUID(), 2, 10.78, 106.70);
        PlanStop first = stop(UUID.randomUUID(), 1, 10.77, 106.69);
        ComputeRouteResponse expected = new ComputeRouteResponse(
                "OPENROUTESERVICE",
                TravelMode.DRIVE,
                1521,
                420,
                List.of(),
                List.of()
        );
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planStopRepository.findByPlanId(planId)).thenReturn(List.of(second, first));
        when(routingClient.computeRoute(eq(TravelMode.DRIVE), anyList())).thenReturn(expected);

        ComputeRouteResponse response = planRouteService.computeRoute(userId, planId, TravelMode.DRIVE);

        assertThat(response).isSameAs(expected);
        verify(planAccessPolicy).assertCanView(plan, userId);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlanStop>> stopsCaptor = ArgumentCaptor.forClass(List.class);
        verify(routingClient).computeRoute(eq(TravelMode.DRIVE), stopsCaptor.capture());
        assertThat(stopsCaptor.getValue()).containsExactly(first, second);
    }

    @Test
    void rejectsStopsWithoutCoordinates() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        Plan plan = Plan.builder().id(planId).createdBy(userId).build();
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planStopRepository.findByPlanId(planId)).thenReturn(List.of(
                stop(UUID.randomUUID(), 1, 10.77, 106.69),
                stop(UUID.randomUUID(), 2, null, 106.70)
        ));

        assertThatThrownBy(() -> planRouteService.computeRoute(userId, planId, TravelMode.DRIVE))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("valid coordinates");
        verify(routingClient, never()).computeRoute(eq(TravelMode.DRIVE), anyList());
    }

    private PlanStop stop(UUID id, Integer orderIndex, Double latitude, Double longitude) {
        return PlanStop.builder()
                .id(id)
                .orderIndex(orderIndex)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
