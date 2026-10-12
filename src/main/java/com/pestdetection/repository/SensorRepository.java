package com.pestdetection.repository;

import com.pestdetection.entity.SensorReading;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorRepository extends JpaRepository<SensorReading, Long> {

    List<SensorReading> findAllByOrderByRecordedAtAsc();

    @Query("select s from SensorReading s where s.trap.id = :trapId order by s.recordedAt asc")
    List<SensorReading> findByTrapIdOrderByRecordedAtAsc(@Param("trapId") Long trapId);

    @Modifying
    @Query("delete from SensorReading s where s.trap.id = :trapId")
    void deleteByTrapId(@Param("trapId") Long trapId);
}
