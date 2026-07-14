package org.example.routingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.routingservice.dto.ComputeRouteRequest;
import org.example.routingservice.dto.ComputeRouteResponse;
import org.example.routingservice.service.RoutingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RoutingController {
    private final RoutingService routingService;

    @PostMapping("/compute")
    public ComputeRouteResponse compute(@Valid @RequestBody ComputeRouteRequest request) {
        return routingService.computeRoute(request);
    }
}
