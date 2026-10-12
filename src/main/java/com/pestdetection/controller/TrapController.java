package com.pestdetection.controller;

import com.pestdetection.entity.SmartTrap;
import com.pestdetection.service.TrapService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traps")
public class TrapController {

    private final TrapService traps;

    public TrapController(TrapService traps) {
        this.traps = traps;
    }

    @GetMapping
    public List<SmartTrap> list() {
        return traps.list();
    }

    @GetMapping("/{id}")
    public SmartTrap get(@PathVariable Long id) {
        return traps.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SmartTrap create(@Valid @RequestBody SmartTrap body) {
        return traps.create(body);
    }

    @PutMapping("/{id}")
    public SmartTrap update(@PathVariable Long id, @Valid @RequestBody SmartTrap body) {
        return traps.update(id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        traps.delete(id);
        return ResponseEntity.noContent().build();
    }
}
