package com.pestdetection.service;

import com.pestdetection.entity.SmartTrap;
import com.pestdetection.exception.ResourceNotFoundException;
import com.pestdetection.repository.AlertRepository;
import com.pestdetection.repository.DetectionRepository;
import com.pestdetection.repository.SensorRepository;
import com.pestdetection.repository.TrapRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TrapService {

    private final TrapRepository traps;
    private final DetectionRepository detections;
    private final SensorRepository sensors;
    private final AlertRepository alerts;
    private final AlertService alertService;

    public TrapService(TrapRepository traps, DetectionRepository detections, SensorRepository sensors,
                       AlertRepository alerts, AlertService alertService) {
        this.traps = traps;
        this.detections = detections;
        this.sensors = sensors;
        this.alerts = alerts;
        this.alertService = alertService;
    }

    @Transactional(readOnly = true)
    public List<SmartTrap> list() {
        return traps.findAll();
    }

    @Transactional(readOnly = true)
    public SmartTrap get(Long id) {
        return traps.findById(id).orElseThrow(() -> new ResourceNotFoundException("Trap", id));
    }

    @Transactional
    public SmartTrap create(SmartTrap in) {
        in.setId(null);
        in.setCreatedAt(Instant.now());
        normalise(in);
        ensureDeviceFree(in.getDeviceId(), null);
        SmartTrap saved = traps.save(in);
        alertService.checkBattery(saved);
        return saved;
    }

    @Transactional
    public SmartTrap update(Long id, SmartTrap in) {
        SmartTrap trap = get(id);
        normalise(in);
        ensureDeviceFree(in.getDeviceId(), id);
        trap.setName(in.getName());
        trap.setDeviceId(in.getDeviceId());
        trap.setLocation(in.getLocation());
        trap.setStatus(in.getStatus());
        trap.setBattery(in.getBattery());
        SmartTrap saved = traps.save(trap);
        alertService.checkBattery(saved);
        return saved;
    }

    /** Removes the trap together with its detections, readings and alerts. */
    @Transactional
    public void delete(Long id) {
        SmartTrap trap = get(id);
        alerts.deleteByTrapId(id);
        detections.deleteByTrapId(id);
        sensors.deleteByTrapId(id);
        traps.delete(trap);
    }

    private void normalise(SmartTrap t) {
        t.setName(t.getName() == null ? null : t.getName().trim());
        String device = t.getDeviceId() == null ? null : t.getDeviceId().trim();
        t.setDeviceId(device == null || device.isEmpty() ? null : device);
        t.setLocation(t.getLocation() == null ? null : t.getLocation().trim());
        if (t.getStatus() == null) t.setStatus("ACTIVE");
        if (t.getBattery() == null) t.setBattery(100);
    }

    private void ensureDeviceFree(String deviceId, Long ownId) {
        if (deviceId == null) return;
        traps.findByDeviceId(deviceId).ifPresent(other -> {
            if (!other.getId().equals(ownId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Device ID " + deviceId + " is already registered");
            }
        });
    }
}
