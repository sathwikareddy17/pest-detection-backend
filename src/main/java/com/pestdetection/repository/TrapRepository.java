package com.pestdetection.repository;

import com.pestdetection.entity.SmartTrap;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrapRepository extends JpaRepository<SmartTrap, Long> {
    Optional<SmartTrap> findByDeviceId(String deviceId);
}
