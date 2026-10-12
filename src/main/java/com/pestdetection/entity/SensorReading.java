package com.pestdetection.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "trap_id", nullable = false)
    private SmartTrap trap;

    // Degrees Celsius.
    @Column(nullable = false)
    private Double temperature;

    // Relative humidity, percent.
    @Column(nullable = false)
    private Double humidity;

    @Column(nullable = false)
    private Instant recordedAt = Instant.now();

    @JsonProperty("trapId")
    public Long getTrapId() { return trap == null ? null : trap.getId(); }

    @JsonProperty("trapName")
    public String getTrapName() { return trap == null ? null : trap.getName(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SmartTrap getTrap() { return trap; }
    public void setTrap(SmartTrap trap) { this.trap = trap; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
