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
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "trap_id")
    private SmartTrap trap;

    // HIGH, MEDIUM or LOW.
    @Column(nullable = false, length = 10)
    private String severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(nullable = false)
    private boolean resolved;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // Identifies the condition that raised the alert, so the same problem is not alerted twice while open.
    @JsonIgnore
    @Column(name = "alert_key", length = 120)
    private String alertKey;

    @JsonProperty("trapId")
    public Long getTrapId() { return trap == null ? null : trap.getId(); }

    @JsonProperty("trapName")
    public String getTrapName() { return trap == null ? null : trap.getName(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SmartTrap getTrap() { return trap; }
    public void setTrap(SmartTrap trap) { this.trap = trap; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getAlertKey() { return alertKey; }
    public void setAlertKey(String alertKey) { this.alertKey = alertKey; }
}
