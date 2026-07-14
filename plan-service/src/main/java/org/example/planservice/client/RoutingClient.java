package org.example.planservice.client;

import org.example.planservice.dto.ComputeRouteResponse;
import org.example.planservice.entity.PlanStop;

import java.util.List;

public interface RoutingClient {
    ComputeRouteResponse computeRoute(TravelMode travelMode, List<PlanStop> stops);
}
