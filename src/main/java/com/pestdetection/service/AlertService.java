package com.pestdetection.service;

import com.pestdetection.entity.Alert;
import com.pestdetection.entity.SensorReading;
import com.pestdetection.entity.SmartTrap;
import com.pestdetection.exception.ResourceNotFoundException;
import com.pestdetection.repository.AlertRepository;
import com.pestdetection.repository.DetectionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the alert rules. A rule raises one alert per problem: while an alert with the same key is still
 * open, the rule stays quiet. Resolving the alert lets the rule fire again.
 */
@Service
public class AlertService {

    static final long PEST_SURGE_THRESHOLD = 10;   // insects of one species at one trap in 24 hours
    static final double HUMIDITY_LIMIT = 88.0;     // percent
    static final double TEMPERATURE_LIMIT = 36.0;  // degrees Celsius
    static final int BATTERY_LIMIT = 20;           // percent

    private final AlertRepository alerts;
    private final DetectionRepository detections;

    public AlertService(AlertRepository alerts, DetectionRepository detections) {
        this.alerts = alerts;
        this.detections = detections;
    }

    @Transactional(readOnly = true)
    public List<Alert> list() {
        return alerts.findAllByOrderByCreatedAtAsc();
    }

    @Transactional
    public Alert markRead(Long id) {
        Alert alert = get(id);
        alert.setRead(true);
        return alerts.save(alert);
    }

    @Transactional
    public Alert resolve(Long id) {
        Alert alert = get(id);
        alert.setResolved(true);
        alert.setRead(true);
        return alerts.save(alert);
    }

    @Transactional
    public void delete(Long id) {
        alerts.delete(get(id));
    }

    // ---- rules, called by the other services after they save data ----

    @Transactional
    public void checkPestSurge(SmartTrap trap, String pestType) {
        Long total = detections.totalSince(trap.getId(), pestType, Instant.now().minus(Duration.ofHours(24)));
        if (total != null && total >= PEST_SURGE_THRESHOLD) {
            raise(trap, "HIGH", pestType + " surge: " + total + " caught in 24 h at " + trap.getName(),
                    "pest|" + trap.getId() + "|" + pestType);
        }
    }

    @Transactional
    public void checkEnvironment(SmartTrap trap, SensorReading reading) {
        if (reading.getHumidity() >= HUMIDITY_LIMIT) {
            raise(trap, "MEDIUM",
                    "Humidity at " + reading.getHumidity() + "%. Conditions favour fungal pests at " + trap.getName(),
                    "hum|" + trap.getId());
        }
        if (reading.getTemperature() >= TEMPERATURE_LIMIT) {
            raise(trap, "HIGH", "Temperature at " + reading.getTemperature() + " C at " + trap.getName(),
                    "temp|" + trap.getId());
        }
    }

    @Transactional
    public void checkBattery(SmartTrap trap) {
        if (!"INACTIVE".equals(trap.getStatus()) && trap.getBattery() != null && trap.getBattery() < BATTERY_LIMIT) {
            raise(trap, "LOW", "Battery at " + trap.getBattery() + "% on " + trap.getName() + ". Swap before next round",
                    "bat|" + trap.getId());
        }
    }

    private void raise(SmartTrap trap, String severity, String message, String key) {
        if (alerts.existsByAlertKeyAndResolvedFalse(key)) {
            return;
        }
        Alert alert = new Alert();
        alert.setTrap(trap);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setAlertKey(key);
        alerts.save(alert);
    }

    private Alert get(Long id) {
        return alerts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alert", id));
    }
}
