package org.example.placeservice.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.dto.PlaceSuggestionResponse;
import org.example.placeservice.service.PlaceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @GetMapping("/autocomplete")
    public List<PlaceSuggestionResponse> autocomplete(
            @RequestParam("query") @NotBlank String query,
            @RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
            @RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double lng,
            @RequestParam(required = false) @Min(1) @Max(10) Integer limit,
            @RequestParam(required = false) String sessionToken
    ) {
        return placeService.autocomplete(query, lat, lng, limit, sessionToken);
    }

    @GetMapping("/details")
    public PlaceDetailsResponse details(
            @RequestParam(defaultValue = "GEOAPIFY") PlaceProvider provider,
            @RequestParam @NotBlank String providerPlaceId
    ) {
        return placeService.details(provider, providerPlaceId);
    }

    @GetMapping("/nearby")
    public List<PlaceDetailsResponse> nearby(
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double lng,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @Min(1) @Max(50_000) Integer radiusMeters,
            @RequestParam(required = false) @Min(1) @Max(10) Integer limit
    ) {
        return placeService.nearby(lat, lng, type, radiusMeters, limit);
    }

    @GetMapping("/viewport")
    public List<PlaceDetailsResponse> viewport(
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double west,
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double south,
            @RequestParam @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double east,
            @RequestParam @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double north,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit
    ) {
        return placeService.viewport(west, south, east, north, type, limit);
    }
}
