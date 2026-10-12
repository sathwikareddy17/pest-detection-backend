package com.pestdetection.dto;

import com.pestdetection.entity.PestDetection;
import java.time.Instant;

public record DetectionResponse(
        Long id,
        Long trapId,
        String trapName,
        String pestType,
        Integer count,
        Double confidence,
        Instant detectedAt) {

    public static DetectionResponse from(PestDetection d) {
        return new DetectionResponse(
                d.getId(),
                d.getTrap().getId(),
                d.getTrap().getName(),
                d.getPestType(),
                d.getQuantity(),
                d.getConfidence(),
                d.getDetectedAt());
    }
}
