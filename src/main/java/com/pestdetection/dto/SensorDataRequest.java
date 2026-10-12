package com.pestdetection.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/** Body for POST /api/sensors. recordedAt defaults to now when omitted. */
public record SensorDataRequest(
        @NotNull(message = "is required") Long trapId,
        @NotNull(message = "is required")
        @DecimalMin(value = "-50.0", message = "is out of range")
        @DecimalMax(value = "80.0", message = "is out of range") Double temperature,
        @NotNull(message = "is required")
        @DecimalMin(value = "0.0", message = "must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "must be between 0 and 100") Double humidity,
        Instant recordedAt) {
}
