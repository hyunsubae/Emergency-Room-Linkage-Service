package com.aivle.mini7.repository;

import com.aivle.mini7.model.HospitalLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HospitalLogRepository extends JpaRepository<HospitalLog, Long> {
    List<HospitalLog> findByLogId(Long logId);
}
