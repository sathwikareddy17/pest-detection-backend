package com.pestdetection.entity;

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
@Table(name = "pest_detections")
public class PestDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trap_id", nullable = false)
    private SmartTrap trap;

    @Column(nullable = false, length = 80)
    private String pestType;

    // Number of insects counted in this detection event.
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    // Model confidence from 0 to 1.
    private Double confidence;

    @Column(nullable = false)
    private Instant detectedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SmartTrap getTrap() { return trap; }
    public void setTrap(SmartTrap trap) { this.trap = trap; }
    public String getPestType() { return pestType; }
    public void setPestType(String pestType) { this.pestType = pestType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
