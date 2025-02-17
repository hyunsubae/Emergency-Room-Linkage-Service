package com.aivle.mini7.dto;

import lombok.Getter;
import lombok.Builder;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;

@Getter
@Builder
public class DashboardStats {
    private long totalCases;              // 전체 케이스 수
    private long emergencyCases;          // 응급 케이스 수
    private double emergencyRatio;        // 응급 비율
    private double avgArrivalTime;        // 평균 도착 시간
    private double arrivalTimeDiff;       // 예상 대비 실제 시간 차이
    private String mostRecommendedHospital; // 가장 많이 추천된 병원
    private long mostRecommendedHospitalCount; // 추천 횟수
    private Map<Integer, Long> hourlyStats;    // 시간대별 통계
    private List<TimeComparisonData> timeComparison; // 시간 비교 데이터

    @Getter
    @AllArgsConstructor
    public static class TimeComparisonData {
        private int estimated;
        private int actual;
    }
} 