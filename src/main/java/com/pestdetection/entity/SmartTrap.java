package com.pestdetection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "smart_traps")
public class SmartTrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    // Hardware identifier printed on the device. Unique when present.
    @Size(max = 60)
    @Column(unique = true, length = 60)
    private String deviceId;

    @Size(max = 200)
    @Column(length = 200)
    private String location;

    @Pattern(regexp = "ACTIVE|INACTIVE|MAINTENANCE", message = "must be ACTIVE, INACTIVE or MAINTENANCE")
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private Integer battery = 100;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getBattery() { return battery; }
    public void setBattery(Integer battery) { this.battery = battery; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
