package com.pestdetection.controller;

import com.pestdetection.dto.SensorDataRequest;
import com.pestdetection.entity.SensorReading;
import com.pestdetection.service.SensorService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final SensorService sensors;

    public SensorController(SensorService sensors) {
        this.sensors = sensors;
    }

    @GetMapping
    public List<SensorReading> list() {
        return sensors.list();
    }

    @GetMapping("/trap/{trapId}")
    public List<SensorReading> byTrap(@PathVariable Long trapId) {
        return sensors.byTrap(trapId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorReading create(@Valid @RequestBody SensorDataRequest request) {
        return sensors.create(request);
    }
}
