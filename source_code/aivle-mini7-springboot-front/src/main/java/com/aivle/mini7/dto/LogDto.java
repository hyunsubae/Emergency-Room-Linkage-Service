package com.aivle.mini7.dto;

import com.aivle.mini7.model.Log;
import com.aivle.mini7.model.HospitalLog;
import com.aivle.mini7.model.FireStationLog;
import lombok.*;
import java.util.List;
import com.aivle.mini7.client.dto.HospitalResponse;
import com.aivle.mini7.client.dto.FireStationResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class LogDto {
    private String datetime;
    private int urgencyLevel;
    private String request;
    private double latitude;
    private double longitude;
    private List<HospitalResponse> hospitals;
    private List<FireStationResponse> fireStations;

    @Getter
    @AllArgsConstructor
    public static class ResponseList {
        private Long id;
        private String request;
        private String datetime;
        private double latitude;
        private double longitude;
        private int urgencyLevel;
        private List<HospitalLog> hospitalLogs;
        private List<FireStationLog> fireStationLogs;
        
        public static ResponseList from(Log log) {
            String formattedDate = "";
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.ENGLISH);
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", java.util.Locale.KOREAN);
                Date date = inputFormat.parse(log.getDatetime());
                formattedDate = outputFormat.format(date);
            } catch (Exception e) {
                formattedDate = log.getDatetime();
            }

            return new ResponseList(
                log.getId(),
                log.getRequest(),
                formattedDate,
                log.getLatitude(),
                log.getLongitude(),
                log.getUrgencyLevel(),
                log.getHospitalLogs(),
                log.getFireStationLogs()
            );
        }
    }
}
