package com.pestdetection.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Body for POST /api/detections. count defaults to 1 and detectedAt to now when omitted. */
public record DetectionRequest(
        @NotNull(message = "is required") Long trapId,
        @NotBlank(message = "is required") @Size(max = 80) String pestType,
        @Min(value = 1, message = "must be at least 1") Integer count,
        @DecimalMin(value = "0.0", message = "must be between 0 and 1")
        @DecimalMax(value = "1.0", message = "must be between 0 and 1") Double confidence,
        Instant detectedAt) {
}
