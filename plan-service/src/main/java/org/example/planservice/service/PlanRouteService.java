package org.example.planservice.service;

import lombok.RequiredArgsConstructor;
import org.example.planservice.client.RoutingClient;
import org.example.planservice.client.TravelMode;
import org.example.planservice.dto.ComputeRouteResponse;
import org.example.planservice.entity.Plan;
import org.example.planservice.entity.PlanStop;
import org.example.planservice.repository.PlanRepository;
import org.example.planservice.repository.PlanStopRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanRouteService {
    private final PlanRepository planRepository;
    private final PlanStopRepository planStopRepository;
    private final PlanAccessPolicy planAccessPolicy;
    private final RoutingClient routingClient;

    public ComputeRouteResponse computeRoute(
            UUID currentUserId,
            UUID planId,
            TravelMode travelMode
    ) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        planAccessPolicy.assertCanView(plan, currentUserId);

        List<PlanStop> stops = planStopRepository.findByPlanId(planId).stream()
                .sorted(Comparator.comparing(
                        PlanStop::getOrderIndex,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .toList();

        if (stops.size() < 2) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "At least two stops are required to compute a route"
            );
        }
        if (stops.stream().anyMatch(this::hasInvalidCoordinates)) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "Every stop must have valid coordinates"
            );
        }

        return routingClient.computeRoute(travelMode, stops);
    }

    private boolean hasInvalidCoordinates(PlanStop stop) {
        Double latitude = stop.getLatitude();
        Double longitude = stop.getLongitude();
        return latitude == null
                || longitude == null
                || !Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || latitude < -90
                || latitude > 90
                || longitude < -180
                || longitude > 180;
    }
}
