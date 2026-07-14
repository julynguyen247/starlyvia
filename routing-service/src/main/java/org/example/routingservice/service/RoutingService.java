package org.example.routingservice.service;

import lombok.RequiredArgsConstructor;
import org.example.routingservice.dto.ComputeRouteRequest;
import org.example.routingservice.dto.ComputeRouteResponse;
import org.example.routingservice.provider.RoutingProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoutingService {
    private final RoutingProvider routingProvider;

    public ComputeRouteResponse computeRoute(ComputeRouteRequest request) {
        return routingProvider.computeRoute(request);
    }
}
