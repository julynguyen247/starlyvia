package org.example.routingservice.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComputeRouteRequestValidationTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requiresAtLeastTwoStops() {
        ComputeRouteRequest request = new ComputeRouteRequest(
                TravelMode.DRIVE,
                List.of(new RouteStopRequest(null, 10.77, 106.69))
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("stops");
    }

    @Test
    void validatesCoordinatesInsideNestedStops() {
        ComputeRouteRequest request = new ComputeRouteRequest(
                TravelMode.WALK,
                List.of(
                        new RouteStopRequest(null, 91.0, 106.69),
                        new RouteStopRequest(null, 10.78, 181.0)
                )
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("stops[0].latitude", "stops[1].longitude");
    }
}
