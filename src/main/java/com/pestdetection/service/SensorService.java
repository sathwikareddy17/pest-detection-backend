package com.pestdetection.service;

import com.pestdetection.dto.SensorDataRequest;
import com.pestdetection.entity.SensorReading;
import com.pestdetection.entity.SmartTrap;
import com.pestdetection.repository.SensorRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorService {

    private final SensorRepository sensors;
    private final TrapService traps;
    private final AlertService alerts;

    public SensorService(SensorRepository sensors, TrapService traps, AlertService alerts) {
        this.sensors = sensors;
        this.traps = traps;
        this.alerts = alerts;
    }

    @Transactional(readOnly = true)
    public List<SensorReading> list() {
        return sensors.findAllByOrderByRecordedAtAsc();
    }

    @Transactional(readOnly = true)
    public List<SensorReading> byTrap(Long trapId) {
        traps.get(trapId);
        return sensors.findByTrapIdOrderByRecordedAtAsc(trapId);
    }

    @Transactional
    public SensorReading create(SensorDataRequest req) {
        SmartTrap trap = traps.get(req.trapId());
        SensorReading r = new SensorReading();
        r.setTrap(trap);
        r.setTemperature(req.temperature());
        r.setHumidity(req.humidity());
        r.setRecordedAt(req.recordedAt() == null ? Instant.now() : req.recordedAt());
        SensorReading saved = sensors.save(r);
        alerts.checkEnvironment(trap, saved);
        return saved;
    }
}
