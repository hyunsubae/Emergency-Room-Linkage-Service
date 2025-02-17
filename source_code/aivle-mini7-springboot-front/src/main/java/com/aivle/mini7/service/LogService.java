package com.aivle.mini7.service;


import com.aivle.mini7.dto.LogDto;
import com.aivle.mini7.model.FireStationLog;
import com.aivle.mini7.model.HospitalLog;
import com.aivle.mini7.model.Log;
import com.aivle.mini7.repository.FireStationLogRepository;
import com.aivle.mini7.repository.HospitalLogRepository;
import com.aivle.mini7.repository.LogRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import com.aivle.mini7.dto.DashboardStats;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.TreeMap;
import jakarta.annotation.PostConstruct;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final LogRepository logRepository;
    private final HospitalLogRepository hospitalLogRepository;
    private final FireStationLogRepository fireStationLogRepository;

    public Page<LogDto.ResponseList> getLogList(Pageable pageable) {
        return logRepository.findAll(pageable)
                .map(LogDto.ResponseList::from);
    }

    public Page<LogDto.ResponseList> getLogList(Pageable pageable, String startDate, String endDate, String urgencyLevel) {
        if (startDate == null && endDate == null && urgencyLevel == null) {
            return getLogList(pageable);
        }

        return logRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (startDate != null && !startDate.isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("datetime"), startDate));
            }
            if (endDate != null && !endDate.isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("datetime"), endDate + " 23:59:59"));
            }
            if ("emergency".equals(urgencyLevel)) {
                predicates.add(cb.lessThanOrEqualTo(root.get("urgencyLevel"), 2));
            } else if ("normal".equals(urgencyLevel)) {
                predicates.add(cb.greaterThan(root.get("urgencyLevel"), 2));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).map(LogDto.ResponseList::from);
    }

    /**
     * 원래 이렇게 나쁜 모듈로 구현하면 안된다.
     * 현재 프로젝트 완료를 위해 급급한 소스이다.
     */
    public void saveLog(LogDto logDTO) {
        Log log = new Log();
        log.setDatetime(logDTO.getDatetime());
        log.setUrgencyLevel(logDTO.getUrgencyLevel());
        log.setRequest(logDTO.getRequest());
        log.setLatitude(logDTO.getLatitude());
        log.setLongitude(logDTO.getLongitude());

        List<HospitalLog> hospitalLogs = logDTO.getHospitals().stream().map(hospital -> {
            HospitalLog hospitalLog = new HospitalLog();
            hospitalLog.setHospitalName(hospital.getHospitalName());
            hospitalLog.setAddress(hospital.getAddress());
            hospitalLog.setEmergencyMedicalInstitutionType(hospital.getEmergencyMedicalInstitutionType());
            hospitalLog.setPhoneNumber1(hospital.getPhoneNumber1());
            hospitalLog.setPhoneNumber3(hospital.getPhoneNumber3());
            hospitalLog.setLatitude(hospital.getLatitude());
            hospitalLog.setLongitude(hospital.getLongitude());
            hospitalLog.setDistance(hospital.getDistance());
            hospitalLog.setEstimatedTime(hospital.getEstimatedTime());
            hospitalLog.setLog(log);
            return hospitalLog;
        }).collect(Collectors.toList());

        List<FireStationLog> fireStationLogs = logDTO.getFireStations().stream().map(fireStation -> {
            FireStationLog fireStationLog = new FireStationLog();
            fireStationLog.setNumber(fireStation.getNumber());
            fireStationLog.setName(fireStation.getName());
            fireStationLog.setAddress(fireStation.getAddress());
            fireStationLog.setHeadquarters(fireStation.getHeadquarters());
            fireStationLog.setPhoneNumber(fireStation.getPhoneNumber());
            fireStationLog.setLatitude(fireStation.getLatitude());
            fireStationLog.setLongitude(fireStation.getLongitude());
            fireStationLog.setType(fireStation.getType());
            fireStationLog.setDistance(fireStation.getDistance());
            fireStationLog.setEstimatedTime(fireStation.getEstimatedTime());
            fireStationLog.setLog(log);
            return fireStationLog;
        }).collect(Collectors.toList());

        log.setHospitalLogs(hospitalLogs);
        log.setFireStationLogs(fireStationLogs);

        logRepository.save(log);
    }

    public List<Log> getAllLogs() {
        return logRepository.findAll();
    }

    public List<HospitalLog> getHospitalLogsByLogId(Long logId) {
        return hospitalLogRepository.findByLogId(logId);
    }

    public List<FireStationLog> getFireStationLogsByLogId(Long logId) {
        return fireStationLogRepository.findByLogId(logId);
    }

    public DashboardStats getDashboardStats() {
        List<Log> allLogs = logRepository.findAll();
        List<HospitalLog> allHospitalLogs = hospitalLogRepository.findAll();
        
        // 전체 케이스 수
        long totalCases = allLogs.size();
        
        // 응급 케이스 수 및 비율
        long emergencyCases = allLogs.stream()
                .filter(log -> log.getUrgencyLevel() <= 2)
                .count();
        double emergencyRatio = totalCases > 0 ? 
                (double) emergencyCases / totalCases * 100 : 0;
        
        // 평균 도착 시간 계산
        double avgActualTime = allHospitalLogs.stream()
                .filter(h -> h.getActualTime() != null)
                .mapToInt(HospitalLog::getActualTime)
                .average()
                .orElse(0);
                
        // 예상 시간과의 차이
        double avgEstimatedTime = allHospitalLogs.stream()
                .mapToInt(HospitalLog::getEstimatedTime)
                .average()
                .orElse(0);
        
        // 가장 많이 추천된 병원
        Map<String, Long> hospitalCounts = allHospitalLogs.stream()
                .collect(Collectors.groupingBy(
                    HospitalLog::getHospitalName, 
                    Collectors.counting()));
        
        Map.Entry<String, Long> mostRecommended = hospitalCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
                
        return DashboardStats.builder()
                .totalCases(totalCases)
                .emergencyCases(emergencyCases)
                .emergencyRatio(Math.round(emergencyRatio * 10) / 10.0)
                .avgArrivalTime(Math.round(avgActualTime * 10) / 10.0)
                .arrivalTimeDiff(Math.round((avgActualTime - avgEstimatedTime) * 10) / 10.0)
                .mostRecommendedHospital(mostRecommended != null ? mostRecommended.getKey() : "-")
                .mostRecommendedHospitalCount(mostRecommended != null ? mostRecommended.getValue() : 0)
                .hourlyStats(calculateHourlyStats(allLogs))
                .timeComparison(calculateTimeComparison(allHospitalLogs))
                .build();
    }

    private Map<Integer, Long> calculateHourlyStats(List<Log> logs) {
        Map<Integer, Long> stats = new TreeMap<>();
        for (int i = 0; i < 24; i++) {
            stats.put(i, 0L);
        }
        
        logs.stream()
            .map(logEntity -> {
                try {
                    String datetime = logEntity.getDatetime();  // Wed Dec 25 21:39:45 KST 2024
                    log.info("Raw DateTime: {}", datetime);
                    
                    String[] parts = datetime.split(" ");
                    // parts[3]이 "21:39:45" 형식
                    int hour = Integer.parseInt(parts[3].split(":")[0]);
                    
                    log.info("Extracted hour: {}", hour);
                    return hour;
                } catch (Exception e) {
                    log.error("DateTime parsing error for {}: {}", logEntity.getDatetime(), e.getMessage());
                    return -1;
                }
            })
            .filter(hour -> hour >= 0 && hour < 24)
            .forEach(hour -> stats.put(hour, stats.get(hour) + 1));

        log.info("Final stats: {}", stats);
        return stats;
    }

    private List<DashboardStats.TimeComparisonData> calculateTimeComparison(List<HospitalLog> logs) {
        return logs.stream()
            .filter(log -> log.getActualTime() != null)
            .map(log -> new DashboardStats.TimeComparisonData(
                log.getEstimatedTime(),
                log.getActualTime()
            ))
            .collect(Collectors.toList());
    }

    public void updateActualTime(Long id, Integer actualTime, String type) {
        if ("hospital".equals(type)) {
            HospitalLog hospitalLog = hospitalLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital log not found"));
            hospitalLog.setActualTime(actualTime);
            hospitalLogRepository.save(hospitalLog);
        } else if ("firestation".equals(type)) {
            FireStationLog fireStationLog = fireStationLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fire station log not found"));
            fireStationLog.setActualTime(actualTime);
            fireStationLogRepository.save(fireStationLog);
        }
    }

    @PostConstruct
    public void initTestData() {
        // 테스트 데이터가 없는 경우��만 추가
        if (logRepository.count() == 0) {
            LogDto testLog = new LogDto();
            testLog.setDatetime("2024-03-20 14:30:00");
            testLog.setUrgencyLevel(2);
            testLog.setRequest("테스트 응급 상황");
            testLog.setLatitude(37.5665);
            testLog.setLongitude(126.9780);
            saveLog(testLog);
        }

        // 실제 소요시간 테스트 데이터
        if (hospitalLogRepository.count() > 0) {
            List<HospitalLog> logs = hospitalLogRepository.findAll();
            for (HospitalLog log : logs) {
                if (log.getActualTime() == null) {
                    log.setActualTime(log.getEstimatedTime() + (int)(Math.random() * 10 - 5));  // 예상시간 ±5분
                    hospitalLogRepository.save(log);
                }
            }
        }
    }
}

