package com.pestdetection.controller;

import com.pestdetection.dto.DetectionRequest;
import com.pestdetection.dto.DetectionResponse;
import com.pestdetection.service.DetectionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/detections")
public class DetectionController {

    private final DetectionService detections;

    public DetectionController(DetectionService detections) {
        this.detections = detections;
    }

    @GetMapping
    public List<DetectionResponse> list() {
        return detections.list();
    }

    @GetMapping("/trap/{trapId}")
    public List<DetectionResponse> byTrap(@PathVariable Long trapId) {
        return detections.byTrap(trapId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DetectionResponse create(@Valid @RequestBody DetectionRequest request) {
        return detections.create(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        detections.delete(id);
        return ResponseEntity.noContent().build();
    }
}
