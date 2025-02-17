package com.aivle.mini7.repository;

import com.aivle.mini7.model.FireStationLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FireStationLogRepository extends JpaRepository<FireStationLog, Long> {
    List<FireStationLog> findByLogId(Long logId);
}
