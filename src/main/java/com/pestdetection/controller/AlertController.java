package com.pestdetection.controller;

import com.pestdetection.entity.Alert;
import com.pestdetection.service.AlertService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alerts;

    public AlertController(AlertService alerts) {
        this.alerts = alerts;
    }

    @GetMapping
    public List<Alert> list() {
        return alerts.list();
    }

    @PutMapping("/{id}/read")
    public Alert markRead(@PathVariable Long id) {
        return alerts.markRead(id);
    }

    @PutMapping("/{id}/resolve")
    public Alert resolve(@PathVariable Long id) {
        return alerts.resolve(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alerts.delete(id);
        return ResponseEntity.noContent().build();
    }
}
