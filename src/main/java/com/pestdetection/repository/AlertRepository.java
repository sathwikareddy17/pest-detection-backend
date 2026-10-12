package com.pestdetection.repository;

import com.pestdetection.entity.Alert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtAsc();

    boolean existsByAlertKeyAndResolvedFalse(String alertKey);

    @Modifying
    @Query("delete from Alert a where a.trap.id = :trapId")
    void deleteByTrapId(@Param("trapId") Long trapId);
}
