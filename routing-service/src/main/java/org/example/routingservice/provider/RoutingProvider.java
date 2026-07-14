package org.example.routingservice.provider;

import org.example.routingservice.dto.ComputeRouteRequest;
import org.example.routingservice.dto.ComputeRouteResponse;

public interface RoutingProvider {
    ComputeRouteResponse computeRoute(ComputeRouteRequest request);
}
