package com.pestdetection.repository;

import com.pestdetection.entity.PestDetection;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetectionRepository extends JpaRepository<PestDetection, Long> {

    List<PestDetection> findAllByOrderByDetectedAtAsc();

    @Query("select d from PestDetection d where d.trap.id = :trapId order by d.detectedAt asc")
    List<PestDetection> findByTrapIdOrderByDetectedAtAsc(@Param("trapId") Long trapId);

    @Modifying
    @Query("delete from PestDetection d where d.trap.id = :trapId")
    void deleteByTrapId(@Param("trapId") Long trapId);

    // Total insects of one pest caught at one trap since a point in time.
    @Query("select coalesce(sum(d.quantity), 0L) from PestDetection d "
            + "where d.trap.id = :trapId and d.pestType = :pest and d.detectedAt >= :since")
    Long totalSince(@Param("trapId") Long trapId, @Param("pest") String pest, @Param("since") Instant since);
}
