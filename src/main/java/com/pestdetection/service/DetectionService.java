package com.pestdetection.service;

import com.pestdetection.dto.DetectionRequest;
import com.pestdetection.dto.DetectionResponse;
import com.pestdetection.entity.PestDetection;
import com.pestdetection.entity.SmartTrap;
import com.pestdetection.exception.ResourceNotFoundException;
import com.pestdetection.repository.DetectionRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetectionService {

    private final DetectionRepository detections;
    private final TrapService traps;
    private final AlertService alerts;

    public DetectionService(DetectionRepository detections, TrapService traps, AlertService alerts) {
        this.detections = detections;
        this.traps = traps;
        this.alerts = alerts;
    }

    @Transactional(readOnly = true)
    public List<DetectionResponse> list() {
        return detections.findAllByOrderByDetectedAtAsc().stream().map(DetectionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DetectionResponse> byTrap(Long trapId) {
        traps.get(trapId);
        return detections.findByTrapIdOrderByDetectedAtAsc(trapId).stream().map(DetectionResponse::from).toList();
    }

    @Transactional
    public DetectionResponse create(DetectionRequest req) {
        SmartTrap trap = traps.get(req.trapId());
        PestDetection d = new PestDetection();
        d.setTrap(trap);
        d.setPestType(req.pestType().trim());
        d.setQuantity(req.count() == null ? 1 : req.count());
        d.setConfidence(req.confidence());
        d.setDetectedAt(req.detectedAt() == null ? Instant.now() : req.detectedAt());
        PestDetection saved = detections.save(d);
        alerts.checkPestSurge(trap, saved.getPestType());
        return DetectionResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        PestDetection d = detections.findById(id).orElseThrow(() -> new ResourceNotFoundException("Detection", id));
        detections.delete(d);
    }
}
